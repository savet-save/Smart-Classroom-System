#include <reg52.h>
#include "stc52ser.h"
#include "ASCII.h"
#include "esp8266.h"

#define BUFFER_MAX_SIZE 99  //��������С
unsigned char buffer[BUFFER_MAX_SIZE];  //�����������ڴ�Ŵ�ESP8266����������Ϣ

//���ӵ�UDP��������ָ��: AT+CIPSTART="UDP","192.168.101.2",9000,8080,0  EOT��SerialPort_SendData���ͽ����ı�ʶ
code unsigned char cmd_connectToUDPServer[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x54, 0x41, 0x52,
																							0x54, 0x3d, 0x22, 0x55, 0x44, 0x50, 0x22, 0x2c, 0x22, 0x31,
																							0x39, 0x32, 0x2e, 0x31, 0x36, 0x38, 0x2e, 0x31, 0x30, 0x31,
																							0x2e, 0x32, 0x22, 0x2c, 0x39, 0x30, 0x30, 0x30, 0x2c, 0x38,
																							0x30, 0x38, 0x30, 0x2c, 0x30, CR, NL, EOT};
//����ָ��AT+CIPSEND
code unsigned char cmd_sendByteDataStart[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x45, 0x4e, 0x44, CR, NL, EOT};
//����ָ��AT+CIPSEND=6
//code unsigned char cmd_sendByteDataStart[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x53, 0x45, 0x4e, 0x44, 0x3d, 0x36, CR, NL, EOT};						

//AT+CIPMODE=1, ��͸ģʽ
code unsigned char cmd_enther_mode[] = {0x41, 0x54, 0x2b, 0x43, 0x49, 0x50, 0x4d, 0x4f, 0x44, 0x45, 0x3d, 0x31, CR, NL, EOT};

//ATE0,�رջ�д
code unsigned char cmd_closeBackWrite[] = {0x41, 0x54, 0x45, 0x30, CR, NL, EOT};

unsigned char beforeBeyt = 0;
unsigned char counter = 0;    //����ESP8266��ֻЩ�������
unsigned char writeIndex = 0;    //������д������

void (*ESP01_Event_WifiConnected)();
void (*ESP01_Event_IpGot)();
void (*ESP01_Event_UDPServerConnected)();


void prepareForData(unsigned char byte);    //��ǰ����


//3. ׼����Ϣ�������ǹ��Ȳ��裬����ȥ��ESP8266���շ���������Ϣͷ
void prepareForData(unsigned char byte){
		writeIndex = byte;
}


//ʶ��ָ�����ʶ����յ�����WIFI CONNECTED������wifi������WIFI IP GOT�����IP������CONNECT�����ӷ�������
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
                SerialPort_Event_ByteReceived = &prepareForData;  //���ӵ�UDP�������󣬽��������
								SerialPort_SendData(cmd_enther_mode);
								Delay100ms(15);
								SerialPort_SendData(cmd_sendByteDataStart);//��ʼ����
							  (*ESP01_Event_UDPServerConnected)();
            }
						break;
				default:
					break;
    }
}

//2. ���￪ʼ�򻺳����洢��Ϣ������ʶ��
void insertBuffer(unsigned char byte){
    if(byte == NL){
        //���յ�β(NL)�󣬽��������Ļ�����Ϣ��ȥʶ��
        parseCmd();
        writeIndex = 0;
        return;
    }
		if(writeIndex >= BUFFER_MAX_SIZE) {
			writeIndex = 0;
		}
    buffer[writeIndex++] = byte;
}


//ͬ.h�е�����
void ESP01_Init(){
	SerialPort_Init_Low();
	SerialPort_Event_ByteReceived = &insertBuffer;  //ע���¼�
	counter = 1;
}

//ͬ.h�е�����
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