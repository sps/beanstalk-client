package us.xph;

public class Job {
    private Integer id;
    private Integer bytes;
    private String[] header;
    private String msg;

    public Job() {
        
    }
    
    public Job(String[] header, Integer id, Integer bytes, String msg) {
        this.header = header;
        this.id = id;
        this.bytes = bytes;
        this.msg = msg;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBytes() {
    	return bytes;
    }

    public void setBytes(Integer bytes) {
    	this.bytes = bytes;
    }

    public String[] getHeader() {
    	return header;
    }

    public void setHeader(String[] header) {
    	this.header = header;
    }

    public String getMsg() {
    	return msg;
    }
    public void setMsg(String msg) {
    	this.msg = msg;
    }
    
}
