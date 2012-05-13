package sweb.server.monitors.stoker.config.json;

public  class Blower {
    String id;
    String name;
    String on;
    
    public Blower() { }
    public Blower( String id, String name, String on ) {
        this.id = id;
        this.name = name;
        this.on = on;
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


    public String getOn()
    {
        return on;
    }


    public void setOn(String on)
    {
        this.on = on;
    }


    public void writeDebug()
    {
        System.out.println("blower - id:" + id + " name:" + name + " on:" + on);
    }
}
