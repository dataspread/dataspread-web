package api.controller;

import api.utils.ResumableInfo;
import api.utils.ResumableInfoStorage;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:63342", "*"})
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

        //Seek to position
        raf.seek((resumableChunkNumber - 1) * (long)info.resumableChunkSize);

        //Save to file
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

            return "All finished.";
        }
        else {
            return null;
        }
    }
}
