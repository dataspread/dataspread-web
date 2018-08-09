package api.controller;

import api.JsonWrapper;
import api.utils.ResumableInfo;
import api.utils.ResumableInfoStorage;
import org.model.AutoRollbackConnection;
import org.model.DBHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zkoss.zss.model.SBook;
import org.zkoss.zss.model.sys.BookBindings;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

@RestController
public class ImportController {
    static final String UPLOAD_DIR = System.getProperty("java.io.tmpdir");

    @PostMapping("/api/importFile")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam("resumableIdentifier") String resumableIdentifier,
                                          @RequestParam("resumableFilename") String resumableFilename,
                                          @RequestParam("resumableChunkSize") int resumableChunkSize,
                                          @RequestParam("resumableTotalSize") long resumableTotalSize,
                                          @RequestParam("resumableTotalChunks") int resumableTotalChunks,
                                          @RequestParam("resumableChunkNumber") int resumableChunkNumber,
                                          @RequestParam("resumableCurrentChunkSize") int resumableCurrentChunkSize)
            throws IOException {

        String resumableFilePath = new File(UPLOAD_DIR, resumableFilename).getAbsolutePath() + ".temp";

        ResumableInfoStorage storage = ResumableInfoStorage.getInstance();
        ResumableInfo info = storage.get(resumableChunkSize, resumableTotalSize,
                resumableIdentifier, resumableFilename, resumableFilePath);

        RandomAccessFile raf = new RandomAccessFile(info.resumableFilePath, "rw");
        raf.seek((resumableChunkNumber - 1) * (long)info.resumableChunkSize);

        InputStream is = file.getInputStream();
        long read = 0;
        long content_length = file.getSize();
        byte[] bytes = new byte[1024 * 100];
        while(read < content_length) {
            int r = is.read(bytes);
            if (r < 0)  {
                break;
            }
            raf.write(bytes, 0, r);
            read += r;
        }
        is.close();
        raf.close();

        info.uploadedChunks.add(new ResumableInfo.ResumableChunkNumber(resumableChunkNumber));
        if (info.checkIfUploadFinished()) { //Check if all chunks uploaded, and change filename
            ResumableInfoStorage.getInstance().remove(info);
            System.out.println("File uploaded to " + UPLOAD_DIR);
            InputStream inputStream = new FileInputStream(new File(UPLOAD_DIR, resumableFilename));
            importBook(inputStream);
            inputStream.close();
            return "All chunks finished.";
        }
        else {
            return null;
        }
    }



    public HashMap<String, Object> importBook(InputStream dataStream){

        String bookName = null;
        String query = null;
        do {
            Random rand = new Random();
            bookName = "book"+rand.nextInt(10000);
            query = "SELECT COUNT(*) FROM books WHERE bookname = ?";
            try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, bookName);
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    if (rs.getInt(1) > 0)
                        bookName=null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return JsonWrapper.generateError(e.getMessage());
            }
        }
        while (bookName==null);

        SBook book = BookBindings.getBookByName(bookName);
        book.checkDBSchema();
        query = "INSERT INTO user_books VALUES (?, ?, 'owner')";
        try (AutoRollbackConnection connection = DBHandler.instance.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "guest");
            statement.setString(2, book.getId());
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return JsonWrapper.generateError(e.getMessage());
        }

        //import into sheet
        char delimiter = ',';
        try {
            book.getSheetByName("Sheet1").getDataModel()
                    .importSheet(new BufferedReader(new InputStreamReader(dataStream)),delimiter,true);
        } catch (IOException e) {
            return JsonWrapper.generateError(e.getMessage());
        }
        //send message
        System.out.println("Imported to book " + book.getBookName());
        return null;
    }


}
