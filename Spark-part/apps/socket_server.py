import socket
import time
import random

# 设置服务器的 IP 和端口
# HOST = '127.0.0.1'
# HOST = 'spark-master'
HOST = 'socket-server'
PORT = 65432


def generate_data():
    start_time = time.time()
    while True:
        if time.time() - start_time > 60:
            break

        x = random.uniform(0, 100)
        y = random.uniform(0, 100)
        data = f"{x},{y}\n"
        yield data
        # time.sleep(0.1)
        # time.sleep(0.05)
        # time.sleep(0.01)

# stop_flag = False
#
# def generate_data():
#     global stop_flag
#     start_time = time.time()
#     while not stop_flag:
#         if time.time() - start_time > 60:  # 传输 1 分钟的数据
#             stop_flag = True  # 设置标志为 True，停止生成数据
#             break
#
#         x = random.uniform(0, 100)
#         y = random.uniform(0, 100)
#         data = f"{x},{y}\n"
#         yield data
#         time.sleep(0.01)


# 启动 Socket 服务器并发送数据
def start_server():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind((HOST, PORT))
    server_socket.listen(1)

    print(f"Server listening on {HOST}:{PORT}...")

    # 接受客户端连接
    client_socket, client_address = server_socket.accept()
    print(f"Connection established with {client_address}")

    data_generator = generate_data()
    while True:
        data = next(data_generator)
        if data is None:  # 如果数据生成结束
            print("1 minute data generation completed, waiting for next connection.")
            time.sleep(1)  # 服务器暂停 1 秒钟，但不会关闭连接
        else:
            client_socket.sendall(data.encode('utf-8'))


    # client_socket.close()
    # server_socket.close()


if __name__ == "__main__":
    start_server()