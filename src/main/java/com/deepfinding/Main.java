
package com.deepfinding;

import com.deepfinding.bean.TopAppInfoBean;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class Main
{
    static boolean monkeyisEnd=false;

    public static void appInfoCollect(String args[]){
        //流的关闭很成问题，外部的流没有被关闭，直接结束了；
        final Process process;
        BufferedWriter bw = null;
        BufferedReader br = null;
        if (args.length != 1 ){
            System.out.println("Usage: get [packagename]!");
            System.exit(-1);
        }
        final String packageName = args[0];//com.brainstorm.begrp

        //child thread to run monkey
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                BufferedReader br = null;
                try {
                    process = Runtime.getRuntime().exec( "cmd.exe /c adb  shell \"monkey -p "+packageName+" --throttle 500 5000 \" ");
                    br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String str;
                    while((str = br.readLine()) != null){
                        System.out.println(str);
                    }
                    process.waitFor();
                    monkeyisEnd = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    if (br!=null) try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //child thread to get Uss info and CPU info
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                BufferedReader br = null;

                File filewrite = new File("D:\\androidtestinfoUss.data");
                FileOutputStream out = null;
                while(!monkeyisEnd){
                    try {
                        /**
                         * 首先读取Uss（系统给程序分配的内存空间，最能说明程序占内存问题的数据）
                         */
                        process = Runtime.getRuntime().exec( "cmd.exe /c adb shell \"procrank|grep " +packageName+
                                "\"");
                        br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        String str;

                        //文件写，将Uss写入到文件中
                        if (!filewrite.exists()) filewrite.createNewFile();
                        out=new FileOutputStream(filewrite,true);
                        while ((str = br.readLine()) != null&&!monkeyisEnd){
                            System.out.println(str);
                            if (str.contains(packageName)){
                                String Uss = str.split(" +")[4];
                                out.write((Uss.substring(0,Uss.length()-1)+" ").getBytes("utf-8"));
                            }
                            //这里可能会报错warn，如果报错还需要处理
                        }
//                        process.destroy();

                        /**
                         * 读取top信息并写入到文件中
                         * 写入信息的格式为：
                         * 前四个User 0%, System 0%, IOW 0%, IRQ 0% 去四个数字
                         * 后面为PID PR CPU% S  #THR     VSS     RSS PCY UID      Name
                         *      3362  0   0% R     1   1912K    600K  fg shell    top
                         * 提取到TopAppInfoBean中，写到文件中时将六个整形数值（ PID+" "+PR+" "+CPUpercent+" "+THR+" "+VSS+" "+RSS）写入文件
                         * 系统命令为 adb -s emulator-5554 shell "top -n 1 |egrep ' [packageName] |System' "
                         * 通过egrep命令只过滤出来上面两行，剩下的均为""串
                         */
                        process = Runtime.getRuntime().exec( "cmd.exe /c adb -s emulator-5554 shell \"top -n 1 |egrep '" +packageName+ "|System' \"");
                        br.close();
                        br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        while ((str = br.readLine()) != null&&!monkeyisEnd){
                            System.out.println(str);
                            if(str.contains("User")){
                                String[] strs = getNumbers(str);
                                out.write((strs[0]+" "+strs[1]+" "+strs[2]+" "+strs[3]+" ").getBytes("utf-8"));
                            }else if(str.contains(packageName)){
                                TopAppInfoBean topAppInfoBean = stringToTopAppInfoBean(str);
                                out.write((topAppInfoBean.getAllIntInfo()+"\r\n").getBytes("utf-8"));
                            }
                        }
//                        process.destroy();//到底要不要destroy，等下问问
                        Thread.sleep(3000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        if (br!=null) try {
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (out !=null){
                            try {
                                out.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
        }).start();
    }
    public static void main( String[] args )
    {
        Main.appInfoCollect(args);
    }

    /**
     * 匹配字符串中的所有数字原始例子如下：User 0%, System 0%, IOW 0%, IRQ 0%
     * 将User system 等四个状态的数据读取出来，放到字符串数组中返回，conten必须是满足上面的格式，否则会出错，该错误判断部分未写，该版本没必要实现,现在已经实现
     * @param content
     * @return
     */
    public static String[] getNumbers(String content) {
        if(!(content.contains("User")&&content.contains("System")&&content.contains("IOW")&&content.contains("IRQ"))) return null;
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(content);
        String[] strarray = new String[4];
        int i = 0;
        while (matcher.find()) {
            strarray[i++] = matcher.group(0);
        }
        return strarray;
    }

    /**
     * 通过top获取的字符串来解析程序状态信息并返回
     * @param content
     * @return
     */
    public static TopAppInfoBean stringToTopAppInfoBean(String content){
        String[] strings = content.split(" +");
        TopAppInfoBean topAppInfoBean = new TopAppInfoBean(Integer.parseInt(strings[1]),Integer.parseInt(strings[2]),
                Integer.parseInt(strings[3].substring(0,strings[3].length()-1)),strings[4],Integer.parseInt(strings[5]),Integer.parseInt(strings[6].substring(0,strings[6].length()-1)),
                Integer.parseInt(strings[7].substring(0,strings[7].length()-1)),strings[8],strings[9],strings[10]);
        return topAppInfoBean;
    }
}
