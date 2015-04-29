package com.deepfinding.bean;

public class TopAppInfoBean {
    int PID;
    int PR;
    int CPUpercent;
    String S;
    int THR;
    int VSS;
    int RSS;
    String PCY;
    String UID;
    String Name;

    public TopAppInfoBean() {
    }

    public TopAppInfoBean(int PID, int PR, int CPUpercent, String s, int THR, int VSS, int RSS, String PCY, String UID, String name) {
        this.PID = PID;
        this.PR = PR;
        this.CPUpercent = CPUpercent;
        S = s;
        this.THR = THR;
        this.VSS = VSS;
        this.RSS = RSS;
        this.PCY = PCY;
        this.UID = UID;
        Name = name;
    }

    public String getAllIntInfo(){
        return PID+" "+PR+" "+CPUpercent+" "+THR+" "+VSS+" "+RSS;
    }
}