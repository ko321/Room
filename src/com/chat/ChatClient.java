package com.chat;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;

public class ChatClient extends Frame {
    TextArea area = new TextArea();             //服务器文本域，显示区域
    TextField field = new TextField();          //输入文本框
    public Socket s = null;                     //客户端指定TCP操作的套接字对象

    public void time(){
        Date date =new Date();
        DateFormat longDateFormat = DateFormat.getDateTimeInstance
                (DateFormat.LONG,DateFormat.LONG);
        area.append(longDateFormat.format(date) + "\n");
    }

    public ChatClient(){                        //类的构造方法
        try{
            s = new Socket("127.0.0.1", 8888);      //创建套接字对象，并将其连接到指定IP的端口
            connectServer();                                    //调用与服务器通信的方法
            ReceiveThread receive = new ReceiveThread();        //创建自定义类的对象receive，读取数据的线程
            Thread thread = new Thread(receive);
            thread.start();                                     //启动线程
        }catch (UnknownHostException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connectServer(){                                //与服务器通信的方法
        area.setEditable(false);                                //显示的文本框不可编辑
        add(area, BorderLayout.CENTER);                         //将组建添加到窗体的指定位置
        add(field, BorderLayout.SOUTH);
        field.addActionListener(new ActionListener() {          //field文本框，添加事件监听器
            @Override
            public void actionPerformed(ActionEvent ae) {
                try{
                    String str = field.getText();               //获取客户端输入的内容
                    if (str.trim().length() == 0)               //输入内容是空
                        return;                                 //是空时，不再执行此方法
                    sendStr(str);                               //输入内容不为空时，发送信息
                    field.setText("");                          //输入文本框，设置为空
                    time();
                    area.append("我: " + str + "\n");                    //输入内容在文本域显示
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        this.addWindowListener(new WindowAdapter() {            //窗体事件监听
            @Override
            public void windowClosing(WindowEvent e) {          //关闭窗体
                System.exit(0);
            }
        });
        setBounds(300, 300, 400, 300);      //设置窗体大小位置
        setVisible(true);                                         //窗体可见
        field.requestFocus();                                     //设置输入框获得焦点
    }

    public void sendStr(String str){                              //发送信息的方法
        try{
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());   //创建向服务器发送信息的数据输出流对象
            dos.writeUTF("client: " + str);                                //发送数据
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void disconnect() throws Exception{
        s.close();                                      //关闭套接字对象
    }

    class ReceiveThread implements Runnable{            //创建实现Runnable接口的类，读取数据
        public void run(){                              //线程体
            if(s==null)
                return;                                 //不是null时才执行此方法
            try{
                DataInputStream dis = new DataInputStream(s.getInputStream());      //创建数据输入流对象dis
                String str = dis.readUTF();             //读取数据
                while (str !=null && str.length() != 0){        //当读取的数据不是null时
                    time();
                    area.append( str + "\n");                    //在客户端的area区域显示读取数据
                    str = dis.readUTF();                        //继续读取数据
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main (String [] args){
        ChatClient client = new ChatClient();                   //创建当前类对象
        client.setTitle("客户端");
        BufferedReader read = new BufferedReader(new InputStreamReader(System.in));
                                                            //读取用户在键盘输入的字符数据对象read
        try{
            String str = read.readLine();                   //输入数据，按行读取
            while(str != null && str.length()!= 0){
                client.sendStr(str);                        //客户端发送数据
                str = read.readLine();                      //继续读取写入数据
            }
            client.disconnect();                            //关闭客户端连接
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
