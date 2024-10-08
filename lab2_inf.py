hem = input()
def repl(n):
    if hem[n]=='1': return hem[:n]+'0'+hem[(n+1):]
    else: return hem[:n]+'1'+hem[(n+1):]
if len(hem)!=7 or (hem.count('0')+hem.count('1')<7):
    print("Неверный ввод значений")
else:
    s1= (int(hem[0])+int(hem[2])+int(hem[4])+int(hem[6]))%2
    s2= (int(hem[1])+int(hem[2])+int(hem[5])+int(hem[6]))%2
    s3= (int(hem[3])+int(hem[4])+int(hem[5])+int(hem[6]))%2
    if s1==0:
        if s2==0:
            if s3 == 1: print('Ошибка в символе r3')
            elif s3==0: print('Ошибок нет')
        else:
            if s3==0: print('Ошибка в символе r2')
            else:
                print('Ошибка в символе i3')
                hem=repl(5)
    else:
        if s2==0:
            if s3 == 1:
                print('Ошибка в символе i2')
                hem= repl(4)
        else:
            if s3==0:
                print('Ошибка в символе i1')
                hem=repl(2)
                
            else:
                print('Ошибка в символе i4')
                hem=repl(6)
    print('Правильное сообщение: '+hem[2]+hem[4]+hem[5]+hem[6])       
