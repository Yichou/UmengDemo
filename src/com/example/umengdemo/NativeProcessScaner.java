package com.example.umengdemo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 用 Linux 的 ps 命令列出系统当前运行的所有进程，
 * 
 * @author yichou 2014-8-5
 *
 */
public class NativeProcessScaner {

    public static class NProc {
        public String user;
        public int pid;
        public int ppid; //父进程id
        public int vsize;
        public int rss;
        public int wchan;
        public int pc;
        public String name;
        
        
        public NProc(String user, int pid, int ppid, int vsize, int rss, int wchan, int pc,
                String name) {
            this.user = user;
            this.pid = pid;
            this.ppid = ppid;
            this.vsize = vsize;
            this.rss = rss;
            this.wchan = wchan;
            this.pc = pc;
            this.name = name;
        }
        
        public void kill() {
            android.os.Process.killProcess(pid);
        }

        public String getUser() {
            return user;
        }

        public int getPid() {
            return pid;
        }

        public int getPpid() {
            return ppid;
        }

        public int getVsize() {
            return vsize;
        }

        public int getRss() {
            return rss;
        }

        public int getWchan() {
            return wchan;
        }

        public int getPc() {
            return pc;
        }

        public String getName() {
            return name;
        }
    }
    
    public static List<String> ps() {
        try {
            Process p = Runtime.getRuntime().exec("ps");
            InputStream is = p.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader reader2 = new BufferedReader(reader);
            
            List<String> list = new ArrayList<String>(128);
            String line = null;
            while((line = reader2.readLine()) != null) {
                list.add(line);
            }
            
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static int toInt(String s) {
        return toInt(s, false);
    }
    
    public static int toInt(String s, boolean hex) {
        try {
            return Integer.parseInt(s, hex? 16 : 10);
        } catch (Exception e) {
        }
        
        return 0;
    }

    public static List<NProc> scanProcess() {
        List<String> list = ps();
        
        if(list != null) {
            final int N = list.size();
            List<NProc> procs = new ArrayList<NProc>(N);

            for(int i=1; i<N; i++) {
                String[] ss = list.get(i).split("\\s+");
                
                NProc proc = new NProc(ss[0], 
                        toInt(ss[1]),
                        toInt(ss[2]),
                        toInt(ss[3]),
                        toInt(ss[4]),
                        toInt(ss[5], true),
                        toInt(ss[6], true),
                        ss[8]);
                procs.add(proc);
            }
            
            return procs;
        }
        
        return null;
    }
}
