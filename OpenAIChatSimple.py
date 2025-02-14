import openai

openai.api_key = "sk-QGP7ircB5lgIszv3vlq2ySuYXUg9XfImqSBhr5PDulnY3Azs"
openai.api_base = "https://api.chatanywhere.com.cn/v1"
messages = []  # 用于存对话记录
datadist={
    'user':'用户'
    ,
    'assistant':'机器人'

}
while True:
    question = input("输入你的问题：")
    if question == "1":
        # 输出对话记录
        for itemdata in messages:
                print(f"{datadist[itemdata['role']] }:{itemdata['content']}", end="\n\n")
        exit()
    messages.append({'role': 'user', 'content': question})
    response = openai.ChatCompletion.create(
        model='gpt-3.5-turbo',
        messages=messages,
        stream=True,
    )

    # 用于存机器人的回答记录
    coding = {
        'role': "assistant",
        'content': ""
    }

    # 遍历机器人的回答
    for event in response:
        try:
            text = event['choices'][0]['delta']['content']
            print(text, end="")
            coding['content'] += text
        except Exception as err:
            '''
            出错是因为机器人的回答到达了结束
                delta字典内容为空
            无需理会这个错误，直接跳过
            '''
            print()
            continue
    # 遍历完了就把回答的记录加到对话总记录里
    messages.append(coding)

'''
对话记录的结构：
messages=[
    {
        'role':"assistant",
        'content':""
}
,
    {
        'role':"user",
        'content':""
}

....

]



'''

