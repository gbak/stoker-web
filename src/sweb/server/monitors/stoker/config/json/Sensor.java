package sweb.server.monitors.stoker.config.json;

public class Sensor {
    
    String id;
    String name;
    String al;
    int ta;
    int th;
    int tl;
    double tc;
    String blower;
    
    public Sensor() { }
    public Sensor( String id, String name, String al, int ta, int th, int tl, double tc, String blower ) {
        this.id = id;
        this.name = name;
        this.al = al;
        this.ta = ta;
        this.th = th;
        this.tl = tl;
        this.tc = tc;
        this.blower = blower;
    }
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAl()
    {
        return al;
    }

    public void setAl(String al)
    {
        this.al = al;
    }

    public int getTa()
    {
        return ta;
    }

    public void setTa(int ta)
    {
        this.ta = ta;
    }

    public int getTh()
    {
        return th;
    }

    public void setTh(int th)
    {
        this.th = th;
    }

    public int getTl()
    {
        return tl;
    }

    public void setTl(int tl)
    {
        this.tl = tl;
    }

    public double getTc()
    {
        return tc;
    }

    public void setTc(double tc)
    {
        this.tc = tc;
    }

    public String getBlower()
    {
        return blower;
    }

    public void setBlower(String blower)
    {
        this.blower = blower;
    }

    
    
    public void writeDebug()
    {
        System.out.println("sensor - id:" + id + " name: " + name + " al:"+ al + "ta:" + ta + " th: " + th + "tl:" + tl + " tc:" + tc + " blower:" + blower );
    }
}
