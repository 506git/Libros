package eco.libros.android.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.socket.client.Socket;

public class CompressZip {
    /**
     * @description 압축 메소드
     * @param path 압축할 폴더 경로
     * @param outputFileName 출력파일명
     * @param mSocket
     */

    // path = /data/user/0/eco.libros.android/files/libros/8fa81d95-250f-40d2-8e31-c3ad58116d94.epub/8fa81d95-250f-40d2-8e31-c3ad58116d94
    // outputPath = /data/user/0/eco.libros.android/files/libros/8fa81d95-250f-40d2-8e31-c3ad58116d94.epub
    // outputFileName = 8fa81d95-250f-40d2-8e31-c3ad58116d94
    public File compress(String path, String outputPath, String outputFileName, Socket mSocket) throws Exception {
        // 파일 압축 성공 여부
        boolean isChk = false;

        File file = new File(path);

        // 파일의 .zip이 없는 경우, .zip 을 붙여준다.
        int pos = outputFileName.lastIndexOf(".") == -1 ? outputFileName.length() : outputFileName.lastIndexOf(".");
        String newName = outputFileName + ".epub";
        // outputFileName .zip이 없는 경우
        if (!outputFileName.substring(pos).equalsIgnoreCase(".zip")) {
            outputFileName += ".zip";
        }

        // 압축 경로 체크
        if (!file.exists()) {
            throw new Exception("Not File!");
        }

        // 출력 스트림
        FileOutputStream fos = null;
        // 압축 스트림
        ZipOutputStream zos = null;

        mSocket.emit("status", "upload");
        try {
            fos = new FileOutputStream(new File(outputPath +"/"+ outputFileName));
            zos = new ZipOutputStream(fos);

            // 디렉토리 검색를 통한 하위 파일과 폴더 검색
            searchDirectory(file, file.getPath(), outputFileName, zos);
//            compressZipFirst(zos);
            // 압축 성공.
            isChk = true;
            return renameFileOne(outputPath, outputFileName, newName);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (zos != null)
                zos.close();
            if (fos != null)
                fos.close();
        }
    }

    /**
     * @description 디렉토리 탐색
     * @param file 현재 파일
     * @param root 루트 경로
     * @param zos  압축 스트림
     */
    private void searchDirectory(File file, String root, String outputFileName, ZipOutputStream zos) throws Exception {
        // 지정된 파일이 디렉토리인지 파일인지 검색
        if (file.isDirectory()) {
            // 디렉토리일 경우 재탐색(재귀)
            File[] files = file.listFiles();
            for (File f : files) {
                if (f.getName().contains("mimetype")){
                    File temp = files[0];
                    files[0] = f;
                    f = temp;
                }
            }
            for (File f : files) {
                System.out.println("file = > " + f);
                searchDirectory(f, root, outputFileName, zos);
            }
        } else {
            // 파일일 경우 압축을 한다.
            try {
                compressZip(file, root, outputFileName, zos);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * @description압축 메소드
     * @param file
     * @param root
     * @param zos
     * @throws Throwable
     */
    private void compressZip(File file, String root, String outputFileName, ZipOutputStream zos) throws Throwable {
        FileInputStream fis = null;
        try {
            String zipName = file.getPath().replace(root + "\\", "");
            outputFileName = outputFileName.substring(0, outputFileName.indexOf(".zip"));
            zipName = zipName.substring(zipName.indexOf(outputFileName) + outputFileName.length() + outputFileName.length()+1 );
//            zipName = file.getPath();
            // 파일을 읽어드림
            fis = new FileInputStream(file);
            // Zip엔트리 생성(한글 깨짐 버그)
//            if (zipName.contains("mimetype")) {
//                return;
//            }
            ZipEntry zipentry = new ZipEntry(zipName);

            // 스트림에 밀어넣기(자동 오픈)
            zos.putNextEntry(zipentry);
            int length = (int) file.length();
            byte[] buffer = new byte[length];
            // 스트림 읽어드리기
            fis.read(buffer, 0, length);
            // 스트림 작성
            zos.write(buffer, 0, length);
            // 스트림 닫기
            zos.closeEntry();

        } catch (Throwable e) {
            throw e;
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    public File renameFileOne(String path, String originFileName, String newFileName){
        File originFile = new File(path+ "/" + originFileName);
        File newFile = new File(path+"/" + newFileName);

        if (originFile == null){
            System.out.println("originFile error");
        }
        if (newFile == null) {
            System.out.println("newFile error");
        }
        if (originFile.renameTo(newFile)){

            System.out.println("test good");
            return newFile;
        }  else {
            System.out.println("test failed");
            return null;
        }

    }

}
