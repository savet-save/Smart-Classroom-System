#ifndef __ESP8266__
#define __ESP8266__

#define SEND_DATA_SIZE 6

extern void (*ESP01_Event_WifiConnected)();    //事件：wifi已连接
extern void (*ESP01_Event_IpGot)();    //事件：IP地址已获得
extern void (*ESP01_Event_UDPServerConnected)();    //事件：已连接到UDP服务器

void ESP01_Init();  //无线模块初始化
void ESP01_ConnectToUDPServer();  //连接UDP服务器

//Send a data of byte sequence end by 'EOT'
void ESP01_SendUDPData(unsigned char* bytes);
void Delay100ms(unsigned char t);
void Delay10ms(unsigned char t);

#endif