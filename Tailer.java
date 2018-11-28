import java.io.File;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;

public class Tailer{
    private File[] files;
    private int lastLines;

    public Tailer(File[] files, int lastLines){
        this.files = files;
        this.lastLines = lastLines;
    }

    private String tialFile(File file) throws Exception{
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        long pos = file.length()-1;
        int lines = lastLines;
        StringBuilder sb = new StringBuilder();
        String line = null;
        while (lines > 0) {
            if(pos <=0){
                break;
            }
            raf.seek(pos--);
            char c;
            c = (char)raf.read();
            sb.append(c);
            if(c =='\n'){
                lines--;
            }
        }
        sb.reverse();

        if (sb.toString().length() > 0 &&sb.toString().charAt(0) == '\n')
            return sb.toString().substring(1);
        else
            return sb.toString();
    }

    public String[] tail() throws Exception{
        String[] output = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            output[i] = LocalDateTime.now().toString() + " " + files[i].getAbsolutePath() + ":\n";
            output[i] += tialFile(files[i]);
        }
        return output;
    }
}