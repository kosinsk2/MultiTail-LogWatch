import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PathFinder{
    private File root;
    private File[] files;
    private String[] patterns;
    private long modifiedTime;
    private ArrayList<File> fileList;


    public PathFinder(String root, String[] patterns, long modifiedTime) throws Exception{
        Path rootPath = Paths.get(root);
        if(Files.exists(rootPath) && Files.isDirectory(rootPath)){
            this.root = new File(root);
        }else{
            throw new Exception("Root path not exits!");
        }
        this.patterns = patterns;
        this.modifiedTime = modifiedTime*1000;
        fileList = new ArrayList<>();

        init();
    }

    public void updateList(){
        init();
    }

    public File[] detectChanges(){
        ArrayList<File> fileList = new ArrayList<>();
        for(File f : files){
           try{
                BasicFileAttributes attr = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                if(attr.lastModifiedTime().toMillis() > System.currentTimeMillis()-modifiedTime){
                    fileList.add(f);
                }
           }catch(Exception e){}
        }
        return fileList.toArray(new File[fileList.size()]);
    }

    private void init(){
        getList(root);
        this.files = filterFiles(fileList);
    }

    private void getList(File root){
        for(File f : root.listFiles()){
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                getList(f);
            }
        }
    }

    private File[] filterFiles(ArrayList<File> fileList){
        ArrayList<File> correctFiles = new ArrayList<>();
        for(File f : fileList){
            for(String regex : patterns){
                if(f.getName().matches(regex)){
                    correctFiles.add(f);
                    break;
                }
            }
        }
        return correctFiles.toArray(new File[correctFiles.size()]);
    }
}