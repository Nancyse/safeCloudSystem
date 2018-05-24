package com.nancyse.controller.NewServer;

import java.io.File;
import java.io.IOException;

public class testMysqlAutoSave {
	/**
     * Java����ʵ��MySQL���ݿ⵼��
     * 
     * @author GaoHuanjie
     * @param hostIP MySQL���ݿ����ڷ�������ַIP
     * @param userName �������ݿ�����Ҫ���û���
     * @param password �������ݿ�����Ҫ������
     * @param savePath ���ݿ⵼���ļ�����·��
     * @param fileName ���ݿ⵼���ļ��ļ���
     * @param databaseName Ҫ���������ݿ���
     * @return ����true��ʾ�����ɹ������򷵻�false��
     */
    public static boolean exportDatabaseTool(String hostIP, String userName, String password, String savePath, String fileName, String databaseName) {
        File saveFile = new File(savePath);
        if (!saveFile.exists()) {// ���Ŀ¼������
            saveFile.mkdirs();// �����ļ���
        }
        if (!savePath.endsWith(File.separator)) {
            savePath = savePath + File.separator;
        }
 
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mysqldump").append(" --opt").append(" -h").append(hostIP);
        stringBuilder.append(" --user=").append(userName) .append(" --password=").append(password).append(" --lock-all-tables=true");
        stringBuilder.append(" --result-file=").append(savePath + fileName).append(" --default-character-set=utf8 ").append(databaseName);
        try {
            Process process = Runtime.getRuntime().exec(stringBuilder.toString());
            if (process.waitFor() == 0) {// 0 ��ʾ�߳�������ֹ��
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
 
    public static void main(String[] args) throws InterruptedException {
        if (exportDatabaseTool("localhost", "root", "root", "E:/backupDatabase", "2014-10-14.sql", "mybatis")) {
            System.out.println("���ݿⱸ�ݳɹ�������");
        } else {
            System.out.println("���ݿⱸ��ʧ�ܣ�����");
        }
    }
}
