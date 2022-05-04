#ifndef __STC52_SER__
#define __STC52_SER__

extern void (*SerialPort_Event_ByteReceived)(unsigned char byte);  //事件：串口接收到字节

void SerialPort_Init_Low();  //初始化11.0592MHz下的9600波特率
void SerialPort_Init_High();  //初始化22.1184MHz下的115200波特率
void SerialPort_SendByte(unsigned char byte);  //发送一个字节
void SerialPort_SendData(unsigned char* bytes);  //发送一组字节

#endif