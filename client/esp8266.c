#include <reg52.h>
#include "stc52ser.h"
#include "ASCII.h"
#include "esp8266.h"

#define BUFFER_MAX_SIZE 99  //缓冲区大小
unsigned char buffer[BUFFER_MAX_SIZE];  //缓冲区：用于存放从ESP8266接收来的消息

//连接到UDP服务器的指令: AT+CIPSTART="UDP","192.168.101.2",9000,8080,0  EOT是SerialPort_SendData发送结束的标识
code unsigned char cmd_connectToUDPServer[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x54, 0x41, 0x52,
																							0x54, 0x3d, 0x22, 0x55, 0x44, 0x50, 0x22, 0x2c, 0x22, 0x31,
																							0x39, 0x32, 0x2e, 0x31, 0x36, 0x38, 0x2e, 0x31, 0x30, 0x31,
																							0x2e, 0x32, 0x22, 0x2c, 0x39, 0x30, 0x30, 0x30, 0x2c, 0x38,
																							0x30, 0x38, 0x30, 0x2c, 0x30, CR, NL, EOT};
//发送指令AT+CIPSEND
code unsigned char cmd_sendByteDataStart[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x45, 0x4e, 0x44, CR, NL, EOT};
//发送指令AT+CIPSEND=6
//code unsigned char cmd_sendByteDataStart[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x45, 0x4e, 0x44, 0x3d, 0x36, CR, NL, EOT};						

//AT+CIPMODE=1, 穿透模式
code unsigned char cmd_enther_mode[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x4d, 0x4f, 0x44, 0x45, 0x3d, 0x31, CR, NL, EOT};

//ATE0,关闭回写
code unsigned char cmd_closeBackWrite[] = {0x41, 0x54, 0x45, 0x30, CR, NL, EOT};

unsigned char beforeBeyt = 0;
unsigned char counter = 0;    //用于ESP8266的只些步骤计数
unsigned char writeIndex = 0;    //缓冲区写所索引

void (*ESP01_Event_WifiConnected)();
void (*ESP01_Event_IpGot)();
void (*ESP01_Event_UDPServerConnected)();


void prepareForData(unsigned char byte);    //提前声明


//3. 准备消息：这里是过度步骤，用于去除ESP8266接收发过来的消息头
void prepareForData(unsigned char byte){
		writeIndex = byte;
}


//识别指令：用于识别接收到的是WIFI CONNECTED（连接wifi）还是WIFI IP GOT（获得IP）还是CONNECT（连接服务器）
void parseCmd(){
    switch(counter){
        case 1:
            if(buffer[0] == 'W' && buffer[5] == 'C'){
                (*ESP01_Event_WifiConnected)();
                counter += 1;
            }
            break;
        case 2:
            if(buffer[0] == 'W' && buffer[5] == 'G'){
                (*ESP01_Event_IpGot)();
                counter += 1;
            }
            break;
        case 3:
            if(buffer[0] == 'A' && buffer[3] == 'C') {
								P1 &= 0x7f;
                counter += 1;
						}
            break;
        case 4:
            if(buffer[0] == 'C' && buffer[3] == 'N' && buffer[6] == 'T'){
                SerialPort_Event_ByteReceived = &prepareForData;  //连接到UDP服务器后，进入第三步
								SerialPort_SendData(cmd_enther_mode);
								Delay100ms(15);
								SerialPort_SendData(cmd_sendByteDataStart);//开始发送
							  (*ESP01_Event_UDPServerConnected)();
            }
						break;
				default:
					break;
    }
}

//2. 这里开始向缓冲区存储消息，用于识别
void insertBuffer(unsigned char byte){
    if(byte == NL){
        //接收到尾(NL)后，将缓冲区的回馈信息送去识别
        parseCmd();
        writeIndex = 0;
        return;
    }
		if(writeIndex >= BUFFER_MAX_SIZE) {
			writeIndex = 0;
		}
    buffer[writeIndex++] = byte;
}


//同.h中的声明
void ESP01_Init(){
	SerialPort_Init_Low();
	SerialPort_Event_ByteReceived = &insertBuffer;  //注册事件
	counter = 1;
}

//同.h中的声明
void ESP01_ConnectToUDPServer(){
  SerialPort_SendData(cmd_connectToUDPServer);
}

void ESP01_SendUDPData(unsigned char* bytes) { 
		unsigned char i = 0;
    for(i = 0; i < SEND_DATA_SIZE; i++) {
			SerialPort_SendByte(bytes[i]);
		}
		P1 &= 0xbf;
}

void Delay1ms()		//11.0592MHz
{
	unsigned char i, j;
	i = 2;
	j = 199;
	do
	{
		while (--j);
	} while (--i);
}

void Delay100ms(unsigned char t){
	unsigned char i, j;
	for(i = 0; i < t; i++){
		for(j = 0; j < 100; j++) {
			Delay1ms();
		}
	}
}

void Delay10ms(unsigned char t){
	unsigned char i, j;
	for(i = 0; i < t; i++){
		for(j = 0; j < 10; j++) {
			Delay1ms();
		}
	}
}