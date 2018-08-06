package com.example.hp.cameracv2;
import android.os.AsyncTask;
import android.util.Log;
import android.util.TimingLogger;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.linear.SparseRealMatrix;
import org.fastica.FastICAException;
import org.opencv.core.Mat;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.fastica.FastICA;


import java.util.Arrays;


public class GetHeartRate extends AsyncTask<String,String,String>{
    private double[]Frequency;
    RealMatrix icasig,I,O,D2,D3;
    private int j;
    public double xyz(Mat[] FrameArray,double framerate){
        int T = 200;
        I = MatrixUtils.createRealIdentityMatrix(T);
        O = new Array2DRowRealMatrix(T-2,1);
        for(int i=0;i<O.getRowDimension();i++){
            for(int j=0;j<O.getColumnDimension();j++){
                O.setEntry(i,j,1);
            }
        }
        double[] a = new double[]{1,-2,1};
        O = O.multiply((new Array2DRowRealMatrix(a)).transpose());
        D2 = spdiags(O,T);
        D3 = (D2.transpose()).multiply(D2);
        double[][] d2a=D2.getData();
        Log.i("filter","rows :"+d2a.length+" cols"+d2a[0].length);
        Log.i("Framerate","average fps is "+framerate);
        double[][] bpmEst=new double[50][2];
        int numofPatchPairs = 50;
        int sHeight=FrameArray[0].height()/40,sWidth=FrameArray[0].width()/40;//small patch size is selected and patch pairs of this size will be produced
        for(int i=0;i<numofPatchPairs;i++){                     //This loop runs once for each patch pair
            TimingLogger timingsLogger = new TimingLogger("Timing2","patch " +i + "timingy");
            int x1=(int)(FrameArray[0].width()*(1/2+Math.random()))/4;
            int y1=(int)(FrameArray[0].height()*(15/7+Math.random()))*7/33;

            int x2=(int)(FrameArray[0].width()*(1/2+Math.random()))/2;
            int y2=(int)(FrameArray[0].height()*(15/7+Math.random()))*7/33;

            double[][] mixedSignal = new double [2][FrameArray.length]; //a 2d array to store the values of the patch pair green values over the frames

            for(int j=0;j<FrameArray.length;j++) {
                //for each patch pair all frames are processed
                //now generate small patches and send to get the average green value over the patch for reach frame
                Mat patch1 = FrameArray[j].submat(y1,y1+sHeight,x1,x1+sWidth);
                Mat patch2 = FrameArray[j].submat(y2,y2+sHeight,x2,x2+sWidth);
                mixedSignal[0][j]=getAverageGreen(patch1);
                mixedSignal[1][j]=getAverageGreen(patch2);
            }

            timingsLogger.addSplit("Moving ave and ica");
            MovingAverage m1=new MovingAverage();
            MovingAverage m2=new MovingAverage();
            mixedSignal[0]=m1.Calculate(mixedSignal[0],framerate/5);
            mixedSignal[1]=m2.Calculate(mixedSignal[1],framerate/5);

//            m1 = new MovingAverage ((int)framerate/5);m2 = new MovingAverage((int)framerate/5);
//            for(int m=0;m<mixedSignal[0].length;m++){
//                mixedSignal[0][m]=m1.next((int)mixedSignal[0][m]);
//                mixedSignal[1][m]=m2.next((int)mixedSignal[1][m]);
//            }

            FastICA fi = null;
            try {
                fi = new FastICA(mixedSignal,2);
            } catch (FastICAException e) {
                Log.e("INSIDE GETHEARTRATE","exception in fastICA");
            }
            double[][] vectors = fi.getICVectors();
            icasig = new Array2DRowRealMatrix(vectors,false);
            Log.i("INSIDE GETHEARTRATE","patch pair created and signal is stored" + i);



            double[] d = new double[2];
            double[][] pxxEst=new double[2][];
            for(j=0;j<2;j++){
                d[j]=minDsum(mixedSignal,icasig.getRow(j));
                final double lambda = 100/(Math.pow(60/framerate,2));
                long startTime=System.nanoTime();
                icasig.setRowMatrix(j, (detrendingFilter(icasig.getRowMatrix(j).transpose(), lambda)).transpose());

                long endTime = System.nanoTime();
                Log.i("Detrending","patch "+i+" signal "+j+" " + (endTime-startTime));
//                for(int m=0;m<199;m++){
//                    Log.i("Heart Rate icasig","icasig "+m+" "+icasig.getRowMatrix(j).getEntry(0,m));
//                }
                icasig.setRowMatrix(j,movingavefliter(icasig.getRowMatrix(j),framerate));

//                icasig.setRowMatrix(j,icasig.getRowMatrix(j).scalarMultiply(0.1));

                pxxEst[j]= pwelch(icasig.getRowMatrix(j),framerate);

                Log.i("Heart Rate 2","frequency at zero : "+Frequency[0]+"\n");
                //index of max entry
                double max=pxxEst[j][1];

                int maxindex=0;
                for(int m=0;m<pxxEst[j].length;m++){
                    if(pxxEst[j][m]>max) {
                        maxindex = m;
                        max=pxxEst[j][m];
                    }
                }
                //Find the peak frequencies in Distribution
                double[] pks=findpeaks(pxxEst[j]);
                if(pks.length==0)
                    continue;
                double max_pk=pks[0];
                double max_pk2=pks[0];
                if(pks.length>1){
                    max_pk2=pks[1];
                    for(int m=0;m<pks.length;m++){
                        if(pks[m]>max_pk){
                            max_pk=pks[m];
                        }
                    }
                    for(int m=0;m<pks.length;m++){
                        if(pks[m]>max_pk2&&pks[m]<max_pk){
                            max_pk2=pks[m];
                        }
                    }
                }
                Log.i("heart rate","max_index="+maxindex+" max_1="+max_pk+" max_2="+max_pk2);
                if(Math.abs(max_pk/max_pk2)>2){
                    bpmEst[i][j]=Math.round(60*Frequency[maxindex]);
                    Log.i("Heart Rate","BPM"+bpmEst[i][j]);
                }
                else
                    bpmEst[i][j]=-1;

            }//end of 2 signals
            timingsLogger.dumpToLog();
            int idx;
            if(d[0]<d[1])
                idx=0;
            else
                idx=1;
            bpmEst[i][idx]=-1;
//            RealMatrix mixSig =new Array2DRowRealMatrix(mixedSignal);
//            now we have stored the values ofget the green averages of the two selected patches and we need to run fastica to get the signal
        }//end of patches
        int count=0;
        for(int k=0;k<50;k++){
            if((bpmEst[k][0]==-1 && bpmEst[k][1]==-1 )|| (bpmEst[k][1]==0&&bpmEst[k][0]==0))
                continue;
            if((bpmEst[k][0]!=-1 && bpmEst[k][0]!=0) || (bpmEst[k][1]!=-1 &&bpmEst[k][1]!=0) )
                count++;
        }

        double[] hr=new double[count];
        count=0;
        for(int k=0;k<50;k++){
            if((bpmEst[k][0]==-1 && bpmEst[k][1]==-1 )|| (bpmEst[k][1]==0&&bpmEst[k][0]==0))
                continue;
            if(bpmEst[k][0]!=-1 && bpmEst[k][0]!=0)
                hr[count++]=bpmEst[k][0];
            if(bpmEst[k][1]!=-1 &&bpmEst[k][1]!=0 )
                hr[count++]=bpmEst[k][1];
        }
        Log.i("HRT ","hrt = "+StatUtils.mean(StatUtils.mode(hr)));
        return StatUtils.mean(StatUtils.mode(hr));
    }


    public double getAverageGreen(Mat patch){
        double average=0;
        for(int i=0;i<patch.width();i++) {
            for (int j = 0; j < patch.height(); j++) {
                average = average + patch.get(i, j)[1];
            }
        }
        return average/(patch.width()*patch.height());
    }



    public RealMatrix detrendingFilter(RealMatrix z,double lambda){
        RealMatrix z_stat;
        z_stat = (I.subtract(MatrixUtils.inverse(I.add(((D3).scalarMultiply(Math.pow(lambda,2)))))));
        long startTime = System.nanoTime();
        z_stat=z_stat.multiply(z);
        long endTime = System.nanoTime();
        Log.i("Multiply time","multiply time="+(endTime-startTime));
        return z_stat;
    }


    public RealMatrix spdiags(RealMatrix O,int T){
        RealMatrix D2 = new Array2DRowRealMatrix(T-2,T);
        for(int i=0;i<T-2;i++){
            for(int j=0;j<T;j++){
                D2.setEntry(i,j,0);
            }
        }
        for(int i=0;i<3;i++){       //column number of O
            for(int j=0;j<T-2;j++){
                D2.setEntry(j,i+j,O.getEntry(j,i));
            }
        }
        return D2;
    }

    public double[] findpeaks(double[] pxx){
        int peaks=0,k=0;
        for(int i=1;i<pxx.length-1;i++){
            if(pxx[i]>pxx[i-1] && pxx[i]>=pxx[i+1])
                peaks++;
        }
        double[] pks = new double[peaks];
        for(int i=1;i<pxx.length-1;i++){
            if(pxx[i]>pxx[i-1] && pxx[i]>=pxx[i+1])
                pks[k++]=pxx[i];
        }
        return pks;
    }


    public double[] pwelch(RealMatrix x,double framerate){

        HammingWindow hammingWindow;            //intialise hamming window
        hammingWindow=new HammingWindow();

        int length = x.getColumnDimension();            //length of the signal

//        int overlapLength = Math.round(length/2);
        // fft length
        double fftLength= Math.pow(2,Math.ceil(Math.log(length)/Math.log(2)));

        //step of each frequency
        double space=1/(fftLength/2+1);
        // frequency vector
        Frequency =new double[(int)fftLength/2+1];
        int m=0;
        for(double k=0;m<(fftLength/2+1);k=k+space){
            Frequency[m]=k*framerate/2;
            m++;
        }

        //getting the hamming window
        double[] window=new double[length];
        for(int k=0;k<length;k++){
            window[k]=(double)hammingWindow.value(length,k);
//            Log.i("Heart Hamming ",window[k]+" "+k);
        }

        RealMatrix Window=new Array2DRowRealMatrix(window);
        double windowcomp = ((Window.transpose()).multiply(Window).getEntry(0,0));
        Log.i("heart rate power","window compensation "+windowcomp);

        double[] x_window=new double[length];
        for(int k=0;k<length;k++){
            x_window[k]=x.getEntry(0,k)*window[k];
        }

        //Calculate fft
        Complex[] Segment = fft(x_window);

        double[] Power=new double[Segment.length];
        for(int k=0;k<Segment.length;k++){
            Power[k]=Math.pow(Segment[k].abs(),2);
            Power[k]=Power[k]/windowcomp;
            Power[k]=Power[k]/framerate;
            if(k>0&&k<Segment.length/2){
                Power[k]=Power[k]*2;

            }
        }
        Power=Arrays.copyOfRange(Power,0, Segment.length/2);



//        RealMatrix Matrix=new Array2DRowRealMatrix(Power);
        return Power;

    }

    static public Complex[] fft(double[] sig) {
        int N = sig.length;
        int M = (int) (Math.log(N) / Math.log(2.0));
        int NpowerTwo = (int) Math.pow(2, M);
        if (NpowerTwo != N) {   // padd the signal with zeros in order to be a power of 2
            NpowerTwo = (int) Math.pow(2, M + 1);
            double[] paddSig = new double[NpowerTwo];
            for (int k = 0; k < N; k++)
                paddSig[k] = sig[k];
            for (int k = N; k < NpowerTwo; k++)
                paddSig[k] = 0.0;
            sig = paddSig;
        }
        Complex[] result;

        FastFourierTransformer fftj = new FastFourierTransformer(DftNormalization.STANDARD);
        result = fftj.transform(sig, TransformType.FORWARD);

        return result;
    }

    public double minDsum (double[][] mixedSig,double[] ica){
        double[][] ica2 = new double[2][ica.length];
        for(int i=0;i<ica.length;i++){
            ica2[0][i]=ica[i];ica2[1][i]=ica[i];
        }
        double s1=0,s2=0;
        for(int i=0;i<ica.length;i++){
            mixedSig[0][i]=Math.pow(mixedSig[0][i]-ica2[0][i],2);
            s1=s1+mixedSig[0][i];
            mixedSig[1][i]=Math.pow(mixedSig[1][i]-ica2[1][i],2);
            s2=s2+mixedSig[1][i];
        }
        return Math.min(s1,s2);
    }

    private RealMatrix movingavefliter(RealMatrix rowMatrix,double framerate) {
        MovingAverage movingAve=new MovingAverage();

        double wlength=framerate/5;
        rowMatrix = new Array2DRowRealMatrix(movingAve.Calculate(rowMatrix.getRow(0),wlength));
        return rowMatrix.transpose();
    }

    @Override
    protected String doInBackground(String... strings) {

        return null;
    }
}
