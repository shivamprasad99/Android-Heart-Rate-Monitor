package com.example.hp.cameracv2;
public class MovingAverage {



    public double[] Calculate(double[] x,double wlength){
        int m=(int)Math.round(wlength);
        int isodd;
        int m2=(m/2);
        int i=0;
        double[] y=new double[x.length];
        if(m%2==0)
            isodd=0;
        else isodd=1;
        x=filter(x,m);

        for( i=0;i<m2+isodd-1;i++){
            y[i]=x[m];
        }
        for(int j=m;j<x.length;j++){
            y[i]=x[j];
            i++;
        }
        while(i<x.length){
            y[i]=x[x.length-1];
            i++;
        }
        return x;
    }

    public double[] filter(double[] x,int size) {    //a=1;

        int i=1;
        double b= 1/size;
        double[] y= new double[x.length];
        y[0]=b*x[0];
        while(i<size){
            y[i]=y[i-1]+b*x[i];
            i++;
        }
        while(i>=size&&i<x.length){
            y[i]=0;
            for(int j=0;j<size;j++){
                y[i]=x[i-j]+y[i];
            }
            y[i]=y[i]/size;
            i++;
        }
        return y;
    }
}
