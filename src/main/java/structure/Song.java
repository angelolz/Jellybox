package structure;

public class Song {
    private String url;
    
    public Song(String songUrl){
        this.url = songUrl;
    }

    public String GetUrl(){
        return this.url;
    }
}
