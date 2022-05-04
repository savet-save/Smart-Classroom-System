#ifndef __ESP8266__
#define __ESP8266__

#define SEND_DATA_SIZE 6

extern void (*ESP01_Event_WifiConnected)();    //�¼���wifi������
extern void (*ESP01_Event_IpGot)();    //�¼���IP��ַ�ѻ��
extern void (*ESP01_Event_UDPServerConnected)();    //�¼��������ӵ�UDP������

void ESP01_Init();  //����ģ���ʼ��
void ESP01_ConnectToUDPServer();  //����UDP������

//Send a data of byte sequence end by 'EOT'
void ESP01_SendUDPData(unsigned char* bytes);
void Delay100ms(unsigned char t);
void Delay10ms(unsigned char t);

#endif