#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <signal.h>

#define PORT 9000
//recv client SOCK_DGARM pack size
#define DATA_SIZE 255
//class has seat num
#define SEAT_NUM_1 68
#define SEAT_NUM_2 56

//class number
#define CLASS_NUM 2

//format
//uchar 0
#define CLASS_HEAD 0xff
//uchar 1 -- data len(max 255)

typedef struct sockaddr_in sockaddr_in_t;

typedef unsigned char uchat;

typedef struct _seat {
	uchat state; // 0 - no people  1 - scheduled  2 - has people
}seat;

// class info
seat class_1[SEAT_NUM_1];
seat class_2[SEAT_NUM_2];
int class_num[CLASS_NUM] = {SEAT_NUM_1, SEAT_NUM_2};
seat* clazz[CLASS_NUM] = {class_1, class_2};

//file
FILE *fp = NULL;
#define FILE_NAME "class_info.dat"

//deal with data from client
int deal_with_data(int sockfd, uchat *data, int size, sockaddr_in_t addr, socklen_t len);
//load
int init_class(void);
//save
int signal_save(void);

int main(int argc , char *argv[])
{
	init_class();
	signal_save();

	int sockfd = socket(AF_INET, SOCK_DGRAM, 0);

	if(sockfd == -1)
	{
		perror("socket");
		return -1;
	}

 	sockaddr_in_t seraddr, cliaddr;
	seraddr.sin_family = AF_INET;
	seraddr.sin_port = htons(PORT);
	//seraddr.sin_addr.s_addr = inet_addr("0.0.0.0");
	seraddr.sin_addr.s_addr = htonl(INADDR_ANY);

	if(bind(sockfd, (struct sockaddr*)&seraddr, sizeof(seraddr)) == -1)
	{
		perror("bind");
		return -1;
	}

	printf("server start -- success\n");


	socklen_t len = sizeof(cliaddr);

	uchat data_buf[DATA_SIZE] = {0};
	ssize_t ret = 0;

	while(1)
	{
		memset(data_buf, 0, DATA_SIZE);
		ret = recvfrom(sockfd, (void *)data_buf, (size_t)sizeof(data_buf), 0,
				(struct sockaddr*)&cliaddr, &len);
		if(ret == -1)//error 
		{
			perror("recv");
			continue;
		}
		printf("ret %d\n", ret);
		deal_with_data(sockfd, data_buf, ret, cliaddr, len);
	}
	
	return 0;
}

// ok -- 0  no -- -1
int judge_format(uchat *data, int size)
{
	if(data == NULL || size <= 0)
	{
		fprintf(stderr, "judge data error\n");
		return -1;
	}
	if(data[0] != CLASS_HEAD)//head error
	{
		return -1;
	}
	if(data[1] != size)//data len error
	{
		return -1;
	}
	return 0;//ok
}

void state_copy(uchat *dst, int dst_size, seat *class)
{
	if(dst == NULL || dst_size <= 0 || class == NULL)
	{
		fprintf(stderr, "state copy parm error\n");
		return;
	}
	int i = 0;
	for(i = 0; i < dst_size; i++)
	{
		dst[i] = class[i].state;
	}
	return;
}

// use in query
int build_send_data(uchat *data, int size, int id)
{
	if(data == NULL || size != DATA_SIZE || id <= 0)//id > 0
	{
		fprintf(stderr, "send data error\n");
		return -1;
	}
	data[0] = CLASS_HEAD;
	if(id > CLASS_NUM)//error
	{
		printf("class id not found\n");
		data[1] = 0x3;
		data[2] = 0xff;
		return 0;
	}
	data[1] = class_num[id - 1] + 2;//start from 1
	state_copy(&data[2], class_num[id - 1], clazz[id - 1]);

	return 0;
}

int query(int sockfd, uchat *data, int size, sockaddr_in_t addr, socklen_t len)
{
	if(data == NULL || size <= 3) 
	{
		fprintf(stderr, "query data error\n");
		return -1;
	}

	int class_id = data[3];
	uchat send_data[DATA_SIZE] = {0};
	build_send_data(send_data, DATA_SIZE, class_id);
	
	ssize_t ret = 0;
	ret = sendto(sockfd, send_data, send_data[1], 0,
			(struct sockaddr*)&addr, len);
#if 0
	//debug info
	printf("query end send to port:%d  ip:%s\n",
			ntohs(addr.sin_port), inet_ntoa(addr.sin_addr));
#endif 
	if(ret == -1)
	{
		perror("send class state error");
		return -1;
	}
	return 0;
}

int reservation(int sockfd, uchat *data, int size, sockaddr_in_t addr, socklen_t len)
{
	if(data == NULL || size < 5)
	{
		fprintf(stderr, "reservation parm error\n");
		return -1;
	}
	int class_id = data[3] - 1;//start from 1
	int opt_id = data[4];
	int seat_id = data[5];

	uchat send_data[DATA_SIZE] = {0};
	send_data[0] = CLASS_HEAD;
	send_data[1] = 0x3;
	if(class_id > CLASS_NUM)//error
	{
		printf("class id not found\n");
		send_data[2] = 0xff;
	}
	if(seat_id >= class_num[class_id])
	{
		send_data[2] = 0xfb;
	}
	
	//change
	if(send_data[2] == 0x00)
	{
		switch(opt_id)
		{
		case 0:
			if(clazz[class_id][seat_id].state == 0)
			{
				clazz[class_id][seat_id].state = 1;
				send_data[2] = 0x00;//ok
			}
			else
			{
				send_data[2] = 0xfe;//error
			}
			break;
		case 1:
			if(clazz[class_id][seat_id].state != 2)
			{
				clazz[class_id][seat_id].state = 0;
				send_data[2] = 0x00;//ok
			}
			else
			{
				send_data[2] = 0xfc;
			}
			break;
		default:
			send_data[2] = 0xfd;
			break;
		}
	}

	ssize_t ret = 0;
	ret = sendto(sockfd, send_data, send_data[1], 0,
			(struct sockaddr*)&addr, len);
	if(ret == -1)
	{
		perror("send reservation error");
		return -1;
	}

	return 0;
}

int on_seat(int sockfd, uchat *data, int size, sockaddr_in_t addr, socklen_t len)
{
	if(data == NULL || size < 5)
	{
		fprintf(stderr, "reservation parm error\n");
		return -1;
	}
	int class_id = data[3] - 1;//start from 1
	int opt_id = data[4];
	int seat_id = data[5];

	uchat send_data[DATA_SIZE] = {0};
	send_data[0] = CLASS_HEAD;
	send_data[1] = 0x3;
	if(class_id > CLASS_NUM)//error
	{
		printf("class id not found\n");
		send_data[2] = 0xff;
	}
	if(seat_id >= class_num[class_id])
	{
		send_data[2] = 0xfb;
	}
	
	//change
	if(send_data[2] == 0x00)
	{
		switch(opt_id)
		{
		case 0:
			clazz[class_id][seat_id].state = 2;
			send_data[2] = 0x00;//ok
			break;
		case 1:
			clazz[class_id][seat_id].state = 0;
			send_data[2] = 0x00;//ok
			break;
		default:
			send_data[2] = 0xfd;
			break;
		}
	}

	ssize_t ret = 0;
	ret = sendto(sockfd, send_data, send_data[1], 0,
			(struct sockaddr*)&addr, len);
	if(ret == -1)
	{
		perror("send reservation error");
		return -1;
	}

	return 0;
}

int deal_with_data(int sockfd, uchat *data, int size, sockaddr_in_t addr, socklen_t len)
{
	if(data == NULL || size < 3)
	{
		fprintf(stderr, "data error\n");
		return -1;
	}

	if(judge_format(data, size) != 0)
	{
		fprintf(stderr, "data format error\n");
		return -1;
	}
	int opt_id = data[2];
	switch(opt_id)
	{
		case 0:
			//query class data


			query(sockfd, data, size, addr, len);
			break;
		case 1:
			//reservation seat
			reservation(sockfd, data, size, addr, len);
			break;
		case 2:
			//on seat
			on_seat(sockfd, data, size, addr, len);
			break;
		default:
			fprintf(stderr, "opt id error\n");
			break;
	}

	
	return 0;
}

int init_class(void)
{
	//load
	fp = fopen(FILE_NAME, "r+");
	if(fp == NULL)
	{
		perror("load error");
		return -1;
	}
	int i, ret;
	for(i = 0; i < CLASS_NUM; i++)
	{
		ret = fread((void *)clazz[i], sizeof(seat), class_num[i], fp);
		if(ret == 0)//end
		{
			return 0;
		}
	}
	fclose(fp);
	return 0;
}

void handle_save(int num)
{
	fp = fopen(FILE_NAME, "w+");
	if(fp == NULL)
	{
		perror("save error");
		return;
	}
	int i, ret;
	for(i = 0; i < CLASS_NUM; i++)
	{
		ret = fwrite((void *)clazz[i], sizeof(seat), class_num[i], fp);
#if 0
		//debug
		int j;
		printf("\n");
		for(j = 0; j < class_num[i]; j++)
		{
			printf("%d ", clazz[i][j].state);
		}
		printf("\n");
#endif
		if(ret == 0)//error
		{
			fprintf(stderr, "write error");
			return;
		}
	}
	printf("\n----- save ok! ----\n");
	printf("---- server exit! ----\n");
	fclose(fp);
	exit(0);
	return;
}

int signal_save(void)
{
	signal(SIGINT, handle_save);
	return 0;
}
