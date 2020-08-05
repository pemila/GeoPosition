package com.pemila.Geohash;

import lombok.Data;

import java.util.Arrays;

/**
 * 根据经纬度获取GeoHash
 * @author pemila
 * @date 2020/8/4 20:07
 **/
public class Main {

    public static void main(String[] args) {
        double[] addr = {116.405724,39.921787};
        GeoHash geoHash = new GeoHash(addr);
        System.out.println(geoHash.getGeoHashStr());
    }
}

@Data
class GeoHash{
    private LocationBean location;

    /** 经纬度转化为geoHash长度*/
    private int hashLength = 8;
    /** 纬度转化为二进制长度*/
    private int latLength = 20;
    /** 经度转化为二进制长度*/
    private int lngLength = 20;

    private double minLat;
    private double minLng;

    /** 32进制转换定义*/
    private static final char[] CHARS = {'0','1','2','3','4','5','6','7','8','9','b','c','d','e','f','g','h','j','k','m','n','p','q','r','s','t','u','v','w','x','y','z'};

    public GeoHash(){}
    public GeoHash(double[] addr) {
        location = new LocationBean(addr);
        initMinLatLng();
    }

    public String getGeoHashStr(){
        return getGeoHashBase32(location.getLat(),location.getLng());
    }

    /** 初始化经纬度最小单位*/
    private void initMinLatLng() {
        minLat = LocationBean.MAX_LAT - LocationBean.MIN_LAT;
        for (int i = 0; i < latLength; i++) {
            minLat /= 2.0;
        }
        minLng = LocationBean.MAX_LNG - LocationBean.MIN_LNG;
        for (int i = 0; i < lngLength; i++) {
            minLng /= 2.0;
        }
    }

    /** 根据经纬度获取GeoHash*/
    private String getGeoHashBase32(double lat, double lng) {
        boolean[] binary = getGeoBinary(lat, lng);
        if (binary == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < binary.length; i += 5) {
            boolean[] base32 = new boolean[5];
            System.arraycopy(binary, i, base32, 0, 5);
            char cha = getBase32Char(base32);
            if (' ' == cha) {
                return null;
            }
            builder.append(cha);
        }
        return builder.toString();
    }

    /** 经纬度转二进制字符串*/
    private boolean[] getGeoBinary(double lat, double lng) {
        boolean[] latArray = getHashArray(lat, LocationBean.MIN_LAT, LocationBean.MAX_LAT, latLength);
        boolean[] lngArray = getHashArray(lng, LocationBean.MIN_LNG, LocationBean.MAX_LNG, lngLength);
        return merge(latArray, lngArray);
    }

    /** 数字转二进制字符串*/
    private boolean[] getHashArray(double value, double min, double max, int length) {
        if (value < min || value > max) {
            return null;
        }
        if (length < 1) {
            return null;
        }
        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            double mid = (min + max) / 2.0;
            System.out.println(min + "  " + mid + "  " + max);
            if (value > mid) {
                result[i] = true;
                min = mid;
            } else {
                result[i] = false;
                max = mid;
            }
        }
        return result;
    }


    public static void main(String[] args) {
        GeoHash geoHash = new GeoHash();
//        boolean[] array = geoHash.getHashArray(34.217209, LocationBean.MIN_LAT, LocationBean.MAX_LAT, 20);
//        boolean[] array = geoHash.getHashArray(108.840014, LocationBean.MIN_LNG, LocationBean.MAX_LNG, 20);

        boolean[] array = geoHash.getGeoBinary(34.217209,108.840014);
        assert array != null;
        String[] ins = new String[array.length];
        Arrays.fill(ins,"1");
        for(int i= 0;i<ins.length;i++){
            ins[i] = array[i]?"1":"0";
        }
        System.out.println(Arrays.toString(ins));


        System.out.println(geoHash.getGeoHashBase32(34.217209,108.840014));
    }




    /** 合并经纬度二进制数组，奇数位为纬度值，偶数位为经度值*/
    private boolean[] merge(boolean[] latArray, boolean[] lngArray) {
        if (latArray == null || lngArray == null) {
            return null;
        }
        boolean[] result = new boolean[lngArray.length + latArray.length];
        Arrays.fill(result, false);
        for (int i = 0; i < lngArray.length; i++) {
            result[2 * i] = lngArray[i];
        }
        for (int i = 0; i < latArray.length; i++) {
            result[2 * i + 1] = latArray[i];
        }
        return result;
    }


    /** 进制转换：2进制-->32进制*/
    private char getBase32Char(boolean[] base32) {
        if (base32 == null || base32.length != 5) {
            return ' ';
        }
        int num = 0;
        for (boolean bool : base32) {
            num <<= 1;
            if (bool) {
                num += 1;
            }
        }
        return CHARS[num % CHARS.length];
    }

}

@Data
class LocationBean {
    public static final double MIN_LAT = -90;
    public static final double MAX_LAT = 90;
    public static final double MIN_LNG = -180;
    public static final double MAX_LNG = 180;
    /** 经度[-180,180]*/
    private double lng;
    /** 纬度[-90,90]*/
    private double lat;

    public LocationBean(double[] addr){
        this.lng = addr[0];
        this.lat = addr[1];
    }
}
