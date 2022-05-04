#ifndef __STC52_SER__
#define __STC52_SER__

extern void (*SerialPort_Event_ByteReceived)(unsigned char byte);  //�¼������ڽ��յ��ֽ�

void SerialPort_Init_Low();  //��ʼ��11.0592MHz�µ�9600������
void SerialPort_Init_High();  //��ʼ��22.1184MHz�µ�115200������
void SerialPort_SendByte(unsigned char byte);  //����һ���ֽ�
void SerialPort_SendData(unsigned char* bytes);  //����һ���ֽ�

#endif