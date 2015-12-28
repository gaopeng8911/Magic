package com.jason.bluetooth.le;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by P_Gao on 2015/12/9.
 */
public class CalculateData {

    //输入值顺序ax,ay,az,gx,gy,gz
    public double[] CalculateAx(double input[]) {
        if (input.length != 10) {
            return null;
        }
        float samplingPeriod = 0.14f;//采样周期,140ms
        float convergenceRate = samplingPeriod * 2;//比例增益支配率收敛到加速度计、磁强计
        float ki = samplingPeriod * 0.005f;//积分增益支配率的陀螺仪偏见的衔接
        float halfT = samplingPeriod * 0.5f;//半周期

        //四元数
        double q0 = input[6];
        double q1 = input[7];
        double q2 = input[8];
        double q3 = input[9];

        //按比例缩小积分误差
        double exInt = 0;
        double eyInt = 0;
        double ezInt = 0;

        double ax = input[0] * 2 * 9.8 / 65536;
        double ay = input[1] * 2 * 9.8 / 65536;
        double az = input[2] * 2 * 9.8 / 65536;
        double gx = input[3] * 250 / 65536;//gx*250°/65536
        double gy = input[4] * 250 / 65536;
        double gz = input[5] * 250 / 65536;

        //测量正常化
        double d = ax * ax + ay * ay + az * az;
        double norm = Math.sqrt(d);
        ax = ax / norm;
        ay = ay / norm;
        az = az / norm;

        //估计方向的重力
        double vx = 2 * (q1 * q3 - q0 * q2);
        double vy = 2 * (q0 * q1 + q2 * q3);
        double vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

        //错误的领域和方向传感器测量参考方向之间的交叉乘积的总和
        double ex = (ay * vz - az * vy);
        double ey = (az * vx - ax * vz);
        double ez = (ax * vy - ay * vx);

        //积分误差比例积分增益
        exInt = exInt + ex * ki;
        eyInt = eyInt + ey * ki;
        ezInt = ezInt + ez * ki;

        //调整后的陀螺仪测量
        gx = gx + convergenceRate * ex + exInt;
        gy = gy + convergenceRate * ey + eyInt;
        gz = gz + convergenceRate * ez + ezInt;

        //四元数积分和正常化
        q0 = q0 + ((0 - q1) * gx - q2 * gy - q3 * gz) * halfT;
        q1 = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT;
        q2 = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT;
        q3 = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT;

        //正常化四元
        norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;
        double pitch = Math.asin(2 * q0 * q2 - 2 * q2 * q3) * 57.3;//俯仰角
        double rool = Math.atan2(2 * (2 * q2 * q3 + 2 * q0 * q1), (1 - 2 * q1 * q1 - 2 * q2 * q2)) * 57.3;//滚动角
        double yaw = Math.atan2(2 * q1 * q2 + 2 * q0 * q3, 1 - 2 * q2 * q2 - 2 * q3 * q3) * 57.3;//航向角

        double[] result = {pitch, rool, yaw, q0, q1, q2, q3};
        if (isNaN(pitch)) {
            double[] result2 = {0, 0, 0, 1, 0, 0, 0};
            return result2;

        }


        return result;

    }

    public double[] CalculateAx2(double input[]) {


        float samplingPeriod = 0.14f;//采样周期,140ms
        float convergenceRate = samplingPeriod * 2;//比例增益支配率收敛到加速度计、磁强计
        float ki = samplingPeriod * 0.005f;//积分增益支配率的陀螺仪偏见的衔接
        float halfT = samplingPeriod * 0.5f;//半周期

        //四元数
        double q0 = input[6];
        double q1 = input[7];
        double q2 = input[8];
        double q3 = input[9];

        //按比例缩小积分误差
        double exInt = 0;
        double eyInt = 0;
        double ezInt = 0;


        for (int i = 0; i < 6; i++) {
            if (input[i] >= 32767) {
                input[i] = 0 - (65536 - input[i]);
            }
        }


        double ax = input[0] * 4 * 9.8 / 65536;
        double ay = input[1] * 4 * 9.8 / 65536;
        double az = input[2] * 4 * 9.8 / 65536;
        double gx = input[3] * 2 * Math.PI * 250 / 65536 / 180;//gx*250°/65536
        double gy = input[4] * 2 * Math.PI * 250 / 65536 / 180;
        double gz = input[5] * 2 * Math.PI * 250 / 65536 / 180;


        //测量正常化
        double d = ax * ax + ay * ay + az * az;
        double norm = Math.sqrt(d);
        ax = ax / norm;
        ay = ay / norm;
        az = az / norm;

        //估计方向的重力
        double vx = 2 * (q1 * q3 - q0 * q2);
        double vy = 2 * (q0 * q1 + q2 * q3);
        double vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

        //错误的领域和方向传感器测量参考方向之间的交叉乘积的总和
        double ex = (ay * vz - az * vy);
        double ey = (az * vx - ax * vz);
        double ez = (ax * vy - ay * vx);

        //积分误差比例积分增益
        exInt = exInt + ex * ki;
        eyInt = eyInt + ey * ki;
        ezInt = ezInt + ez * ki;

        //调整后的陀螺仪测量
        gx = gx + convergenceRate * ex + exInt;
        gy = gy + convergenceRate * ey + eyInt;
        gz = gz + convergenceRate * ez + ezInt;

        //四元数积分和正常化
        q0 = q0 + ((0 - q1) * gx - q2 * gy - q3 * gz) * halfT;
        q1 = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT;
        q2 = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT;
        q3 = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT;

        //正常化四元
        norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;
        double pitch = Math.asin(2 * q0 * q2 - 2 * q1 * q3) * 57.3;//俯仰角
        double rool = Math.atan2((2 * q2 * q3 + 2 * q0 * q1), (1 - 2 * q1 * q1 - 2 * q2 * q2)) * 57.3;//滚动角
        double yaw = Math.atan2(2 * q1 * q2 + 2 * q0 * q3, 1 - 2 * q2 * q2 - 2 * q3 * q3) * 57.3;//航向角


        double[] result = {pitch, rool, yaw, q0, q1, q2, q3};
        if (isNaN(pitch)) {
            double[] result2 = {0, 0, 0, 1, 0, 0, 0};
            return result2;

        }
        return result;

    }

    public double[] CalculateMoveX(double input[]) {
        float samplingPeriod = 0.14f;//采样周期,140ms
        float convergenceRate = samplingPeriod * 2;//比例增益支配率收敛到加速度计、磁强计
        float ki = samplingPeriod * 0.005f;//积分增益支配率的陀螺仪偏见的衔接
        float halfT = samplingPeriod * 0.5f;//半周期
        double lx = input[10];
        double ly = input[11];


        //四元数
        double q0 = input[6];
        double q1 = input[7];
        double q2 = input[8];
        double q3 = input[9];

        //按比例缩小积分误差
        double exInt = 0;
        double eyInt = 0;
        double ezInt = 0;


        for (int i = 0; i < 6; i++) {
            if (input[i] >= 32767) {
                input[i] = 0 - (65536 - input[i]);
            }
        }


        double ax = input[0] * 4 * 9.8 / 65536;
        double ay = input[1] * 4 * 9.8 / 65536;
        double az = input[2] * 4 * 9.8 / 65536;
        double gx = input[3] * 2 * Math.PI * 250 / 65536 / 180;//gx*250°/65536
        double gy = input[4] * 2 * Math.PI * 250 / 65536 / 180;
        double gz = input[5] * 2 * Math.PI * 250 / 65536 / 180;


        //测量正常化
        double d = ax * ax + ay * ay + az * az;
        double norm = Math.sqrt(d);
        ax = ax / norm;
        ay = ay / norm;
        az = az / norm;

        //估计方向的重力
        double vx = 2 * (q1 * q3 - q0 * q2);
        double vy = 2 * (q0 * q1 + q2 * q3);
        double vz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

        //错误的领域和方向传感器测量参考方向之间的交叉乘积的总和
        double ex = (ay * vz - az * vy);
        double ey = (az * vx - ax * vz);
        double ez = (ax * vy - ay * vx);

        //积分误差比例积分增益
        exInt = exInt + ex * ki;
        eyInt = eyInt + ey * ki;
        ezInt = ezInt + ez * ki;

        //调整后的陀螺仪测量
        gx = gx + convergenceRate * ex + exInt;
        gy = gy + convergenceRate * ey + eyInt;
        gz = gz + convergenceRate * ez + ezInt;

        //四元数积分和正常化
        q0 = q0 + ((0 - q1) * gx - q2 * gy - q3 * gz) * halfT;
        q1 = q1 + (q0 * gx + q2 * gz - q3 * gy) * halfT;
        q2 = q2 + (q0 * gy - q1 * gz + q3 * gx) * halfT;
        q3 = q3 + (q0 * gz + q1 * gy - q2 * gx) * halfT;

        //正常化四元
        norm = Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);
        q0 = q0 / norm;
        q1 = q1 / norm;
        q2 = q2 / norm;
        q3 = q3 / norm;
        double mx = 0 - 2 * (q1 * q2 + q0 * q3) / (q0 * q0 + q1 * q1 - q2 * q2 - q3 * q3);//x轴相对位移
        double my = 2 * (q1 * q3 + q0 * q2) / (q0 * q0 + q1 * q1 - q2 * q2 - q3 * q3);//y轴相对位移

        lx = lx + mx;//X轴位移累加
        ly = ly + mx;//Y轴位移累加

        double[] result = {q0, q1, q2, q3, lx, ly};


        return result;
    }


    public double[] CalculateAxQiu(double input[]) {

        //以一定时间间隔作为单位，去每个区间内的最大值和最小值，选出差距最大的一个角度；
//       /若最大的角度差大于某一个阀值，则认为发生了对应的拳法


        return null;
    }

    //获取ArrayList中的最大值
    public double ArrayListMax(ArrayList list) {
        try {
            double maxDevation = 0.0;
            int totalCount = list.size();
            if (totalCount >= 1) {
                double max = Double.parseDouble(list.get(0).toString());
                for (int i = 0; i < totalCount; i++) {
                    double temp = Double.parseDouble(list.get(i).toString());
                    if (temp > max) {
                        max = temp;
                    }
                }
                maxDevation = max;
            }
            return maxDevation;
        } catch (Exception ex) {
            return 0;
        }

    }

    //获取ArrayList中的最小值
    public double ArrayListMin(ArrayList list) {
        try {
            double mixDevation = 0.0;
            int totalCount = list.size();
            if (totalCount >= 1) {
                double min = Double.parseDouble(list.get(0).toString());
                for (int i = 0; i < totalCount; i++) {
                    double temp = Double.parseDouble(list.get(i).toString());
                    if (min > temp) {
                        min = temp;
                    }
                }
                mixDevation = min;
            }
            return mixDevation;
        } catch (Exception ex) {
            return 0;
        }

    }


    public boolean isNaN(double v) {
        return (v != v);
    }

    public int[] DataCleaning(String input) {
        int[] result = {0, 0, 0, 0, 0, 0};
        String reg = "([a-z]{2}=[0-9]{6}, ){6}";
        String str1 = "ax=002397, ay=065489, az=056810, gx=065261, gy=000162, gz=000059, ";
        String str2 = "ax=002397, ";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String[] strs = input.split(", ");
            result[0] = Integer.parseInt(strs[0].substring(3, 9));
            result[1] = Integer.parseInt(strs[1].substring(3, 9));
            result[2] = Integer.parseInt(strs[2].substring(3, 9));
            result[3] = Integer.parseInt(strs[3].substring(3, 9));
            result[4] = Integer.parseInt(strs[4].substring(3, 9));
            result[5] = Integer.parseInt(strs[5].substring(3, 9));

        }
        return result;
    }

    public double[] DataCleaningForDevice(String input, String deviceName) {


        double[] result = {0, 0, 0, 0, 0, 0};
        String reg = "([a-z]{2}=[0-9]{6}, ){6}";
        String str1 = "ax=002397, ay=065489, az=056810, gx=065261, gy=000162, gz=000059, ";
        String str2 = "ax=002397, ";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            String[] strs = input.split(", ");
            result[0] = Integer.parseInt(strs[0].substring(3, 9));
            result[1] = Integer.parseInt(strs[1].substring(3, 9));
            result[2] = Integer.parseInt(strs[2].substring(3, 9));
            result[3] = Integer.parseInt(strs[3].substring(3, 9));
            result[4] = Integer.parseInt(strs[4].substring(3, 9));
            result[5] = Integer.parseInt(strs[5].substring(3, 9));

        }

        if (deviceName == "ChinaMobile") {
            result[0] = result[0];// + 419;
            result[1] = result[1];// - 326.262;
            result[2] = result[2];// + 7830;
            result[3] = result[3];//- 107.023;
            result[4] = result[4];//+ 3.5881;
            result[5] = result[5];//+ 419;

        } else if (deviceName == "China Mobile1") {
            result[0] = result[0];//- 22.9884;
            result[1] = result[1];//- 745.392;
            result[2] = result[2];// + 7320;
            result[3] = result[3];//- 147.062;
            result[4] = result[4];//+ 419;
            result[5] = result[5];//- 17.0579;
        } else if (deviceName == "China Mobile4") {
            result[0] = result[0];//+ 149.4457;
            result[1] = result[1];//- 487.376;
            result[2] = result[2];// + 7570;
            result[3] = result[3];//- 171.243;
            result[4] = result[4];//+ 107.595;
            result[5] = result[5];//+ 20.4604;
        }


        return result;
    }


    public double getAngular(double input) {
        double result = 0;
        if (input >= 32768) {
            result  = 0- (65536-input)*250/32768;
        } else {
            result = input*250/32768;
        }

        return result;
    }

    public double getComplement(double input){
        double result = 0;
        if(input>=32768){
            result = 0-(65536-input);
        }else{
            result = input;
        }
        return result;
    }


}
