package com.chat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class ChatServer extends Frame {
    TextArea area = new TextArea();             //服务器文本域
    TextField field = new TextField();          //输入文本框
    ServerSocket server = null;                 //声明服务器套接字对象
    Collection clients = new ArrayList();       //定义存放线程的容器

    public ChatServer(int prot){
        try{
            server = new ServerSocket(prot);    //创建绑定到指定窗口的服务器套接字
            connectClient();                    //与客户端建立通信连接
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void time(){
        Date date =new Date();
        DateFormat longDateFormat = DateFormat.getDateTimeInstance
                (DateFormat.LONG,DateFormat.LONG);
        area.append(longDateFormat.format(date) + "\n");
    }

    private void connectClient() {              //与客户端通信
        area.setEditable(false);                //设置area文本框组件不可更改
        add(area, BorderLayout.CENTER);         //文本域组件添加到窗体中间
        add(field, BorderLayout.SOUTH);         //套接字对象在窗体下边
        this.addWindowListener(new WindowAdapter() {            //窗体事件
            @Override
            public void windowClosing(WindowEvent e) {          //关闭窗体
                System.exit(0);
            }
        });
        setBounds(0, 0, 400, 300);        //设置窗体的位置
        setVisible(true);                                       //设置窗体可见
    }

    public  void startServer(){                                 //启动方法
        while(true){
            try{
                Socket s = server.accept();                     //获取客户端的连接套接字对象
                ClientConn con = new ClientConn(s);             //创建线程对象
                clients.add(con);                               //将创建的对象添加到容器
                time();
                area.append("客户端已上线\n" );
                System.out.println("客户端" + s.getInetAddress() + ":" + s.getPort());
                System.out.println("建立连接\n" );
                //area.append("客户端" + s.getInetAddress() + ":" + s.getPort());        //在服务器的文本域中显示字符串
                //area.append("建立连接\n" );
                field.addActionListener(new ActionListener() {              //文本域事件，向指定客户端发送数据
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        try{
                            String str = field.getText();                   //获取客户端输入的内容
                            if (str.trim().length() == 0)                   //判断是否为空
                                return;                                     //如果为空，则不再执行此方法
                            con.sendStr(str);                               //输入内容不是空时，发送消息
                            field.setText("");                              //输入文本框，设置为空
                            time();
                            area.append("我: " + str + "\n");                        //输入内容在文本域显示
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    class ClientConn implements Runnable{                   //创建线程类
        Socket s = null;                                    //声明客户套接字对象

        public ClientConn(Socket s){                        //构造方法
            this.s = s;
            Thread thread = new Thread(this);       //创建当前类线程并启动
            thread.start();
        }

        public void sendStr(String str){                    //发送消息
            try{
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());   //创建数据输出流对象
                dos.writeUTF("server: " + str );        //输出数据
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        public void dispose(){                              //线程销毁，即关闭当前客户端
            try{
                if(s != null)                               //若客户端的套接字对象不是null
                    s.close();                              //关闭客户端套接字对象
                clients.remove(this);                   //在容器中删除当前线程
                area.append("客户端下线 \n");               //在文本域中显示删除线程信息，即一个客户端退出
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void run() {                                 //线程体
            try {
                DataInputStream dis = new DataInputStream(s.getInputStream());          //数据流输入对象dis
                String str = dis.readUTF();                                    //读取数据
                while (str != null && str.length() != 0) {                     //判断读取数据
                    time();
                    area.append(str + "\n");                                   //在服务器端显示客户端数据
                    str = dis.readUTF();                                       //继续读取数据
                }
                this.dispose();                                                //关闭当前线程，即客户端
                ;
            } catch (Exception e) {
                System.out.println("客户端退出");
                this.dispose();
            }
        }
    }
    public static void main(String[] args) throws Exception{
        ChatServer server = new ChatServer(8888);                   //创建当前类对象，并指定要连接的端口
        server.setTitle("服务器端");
        server.startServer();                                             //启动服务器端
    }
}
