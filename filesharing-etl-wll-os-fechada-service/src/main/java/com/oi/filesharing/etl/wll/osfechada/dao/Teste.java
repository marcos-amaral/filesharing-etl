/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oi.filesharing.etl.wll.osfechada.dao;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.oi.filesharing.etl.lib.config.FileSharingConfig;
import com.oi.filesharing.etl.lib.dao.FtpConnectionException;
import com.oi.filesharing.etl.lib.smb.SmbConnection;
import com.oi.filesharing.etl.lib.smb.SmbOperation;
import com.oi.filesharing.etl.wll.osfechada.config.WllOsFechadaConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author mmouraam
 */
public class Teste {

    private static String FILE_NAME = "ADIMPLENTES_CREDITO_SAIDA_URA_20210331_073016.csv";

    public static void main(String[] args) {
        String s = "a|b|c";
        List<String> list = new ArrayList<>();
        for (String string : s.split("\\|")) {
            list.add(string);
        }
        
        String[] variables_nivel_1 = new String[list.size()];
        
        list.toArray(variables_nivel_1);
        
        System.out.println(Arrays.asList(variables_nivel_1));
    }
    
    public static void main3(String[] args) throws IOException, FtpConnectionException, Exception {
        WllOsFechadaConfig fileSharingConfig = new WllOsFechadaConfig();
        fileSharingConfig.setHost("10.60.0.211");
        fileSharingConfig.setUser("marcos.amaral.ev");
        fileSharingConfig.setPassword("Mar11356");
        fileSharingConfig.setDomain("VALUETEAM");
        System.out.println(fileSharingConfig);
        
        SmbConnection fileSharingConnection = new SmbConnection(fileSharingConfig);
        SmbOperation fileSharingOperation = new SmbOperation((Session) fileSharingConnection.getConnection(),"ddMMyy","D");
        
        InputStream retrieveFileStream = fileSharingOperation.retrieveFileStream(((WllOsFechadaConfig) fileSharingConfig).getPath(), ((WllOsFechadaConfig) fileSharingConfig).getFiles()[0],-1);
        List<String> processMailing = processMailingCsv(retrieveFileStream);
        System.out.println(processMailing);
        
    }
    
    public static void main2(String[] args) throws IOException {
        InputStream mailingFileLocal = getMailingFileSmb();
        List<String> processMailing = processMailingCsv(mailingFileLocal);
        System.out.println(processMailing);
    }

    private static List<String> processMailingCsv(InputStream file) throws IOException {
        List<String> mailing = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(file));

            // skip the header of the csv
            mailing = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
            br.close();

        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        return mailing;
    }

    private static List<String> processMailingXlsx(InputStream file) throws IOException {
        List<String> mailing = new ArrayList<>();

        try {

            Workbook workbook = new XSSFWorkbook(file);
            Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();

                mailing.add(currentRow.getCell(3).getStringCellValue());

//                Iterator<Cell> cellIterator = currentRow.iterator();
//                while (cellIterator.hasNext()) {
//
//                    Cell currentCell = cellIterator.next();
//                    //getCellTypeEnum shown as deprecated for version 3.15
//                    //getCellTypeEnum ill be renamed to getCellType starting from version 4.0
//                    if (currentCell.getCellType() == CellType.STRING) {
//                        System.out.print(currentCell.getStringCellValue() + "--");
//                    } else if (currentCell.getCellType() == CellType.NUMERIC) {
//                        System.out.print(currentCell.getNumericCellValue() + "--");
//                    }
//
//                }
            }
        } catch (FileNotFoundException e) {
            throw e;

        } catch (IOException e) {
            throw e;

        }

        return mailing;
    }

    private static InputStream getMailingFileLocal() throws IOException {
        File file = new File("C:\\dev\\Docs\\Motor de regras\\Wll Os Fechada\\mailings\\" + FILE_NAME);

        return new FileInputStream(file);
    }

    private static InputStream getMailingFileFtp() throws FileNotFoundException, IOException {
        InputStream inputStream = null;
        FTPClient ftp = new FTPClient();

        try {
            FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
            ftp.configure(config);

            ftp.connect("200.202.193.142");

            System.out.println("Connected to 200.202.193.142 on " + (ftp.getDefaultPort()));
            int reply = ftp.getReplyCode();

            System.out.println("reply: " + reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }

            if (!ftp.login("Urapos", "Oi@@2016")) {
                ftp.disconnect();
                System.err.println("FTP server refused login.");
                System.exit(1);
            }

            ftp.enterLocalPassiveMode();
            ftp.setDefaultTimeout(3000);
            ftp.setDataTimeout(3001);
            ftp.setSoTimeout(3002);
            ftp.setConnectTimeout(3003);

            System.out.println("Remote system is " + ftp.getSystemType());

            //ftp.changeWorkingDirectory("Documentos_URA/10331/20210420/");
            System.out.println("changed directory");
            System.out.println("status: " + ftp.getStatus());
            System.out.println("timeout: " + ftp.getConnectTimeout());

            System.out.println("Listando arquivos: " + ftp.listFiles("Documentos_URA/10331/20210420").length);

            for (final FTPFile f : ftp.listFiles("Documentos_URA/10331/20210420/")) {
                System.out.println(">" + f.getRawListing());
                System.out.println(f.getName());
                System.out.println(f.getSize());
                System.out.println(f.getTimestamp().getTime());

                if (f.getName().equals(FILE_NAME)) {
                    inputStream = ftp.retrieveFileStream("Documentos_URA/10331/20210420/" + FILE_NAME);
                    break;
                }

            }

            ftp.noop(); // check that control connection is working OK

            ftp.logout();

        } catch (FileNotFoundException ex) {
            throw ex;

        } catch (IOException ex) {
            throw ex;

        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (final IOException f) {
                    throw f;
                }
            }
        }

        return inputStream;
    }

    private static InputStream getMailingFileSmb() throws IOException {
        InputStream inputStream = null;

        SmbConfig config = SmbConfig.builder()
                .withTimeout(30, TimeUnit.SECONDS) // Timeout sets Read, Write, and Transact timeouts (default is 60 seconds)
                .withSoTimeout(30, TimeUnit.SECONDS) // Socket Timeout (default is 0 seconds, blocks forever)
                .build();

        SMBClient client = new SMBClient(config);

        Session session = null;
        Connection connection = client.connect("10.60.0.211");
        AuthenticationContext ac = new AuthenticationContext("marcos.amaral.ev", "Mar11356".toCharArray(), "VALUETEAM");
        session = connection.authenticate(ac);

        DiskShare share = (DiskShare) session.connectShare("Users");
        String filePath = "Public\\Documents\\INADIMPLENTES_BAIXA_SALDO_SAIDA_URA_20210407_151500.csv";

        for (FileIdBothDirectoryInformation object : share.list("Public\\Documents\\")) {
            System.out.println(object.getFileName());
        }

        
        Set<SMB2ShareAccess> shareAccess = new HashSet<>();
        shareAccess.addAll(SMB2ShareAccess.ALL);

        Set<SMB2CreateOptions> createOptions = new HashSet<>();
        createOptions.add(SMB2CreateOptions.FILE_WRITE_THROUGH);

        Set<AccessMask> accessMaskSet = new HashSet<>();
        accessMaskSet.add(AccessMask.GENERIC_ALL);
        com.hierynomus.smbj.share.File file;

        file = share.openFile(filePath, accessMaskSet, null, shareAccess, SMB2CreateDisposition.FILE_OPEN, createOptions);
        inputStream = file.getInputStream();

        return inputStream;
    }

    private static Function<String, String> mapToItem = (line) -> {

        String[] p = line.split(";");// a CSV has comma separated lines

        String item = new String();

        if (p[3] != null && p[3].trim().length() > 0) {
            item = String.valueOf((p[3]));
        }
        //more initialization goes here

        return item;
    };
}
