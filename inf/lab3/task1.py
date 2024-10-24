'''
EYE = [
NOSE = <
ROT = )
'''
import re
smile = r'\[\<\)'
print('Кол-во смайлов: '+ str(len(re.findall(smile,'ничего'))))
print('Кол-во смайлов: '+ str(len(re.findall(smile,'смайл[<)'))))
print('Кол-во смайлов: '+ str(len(re.findall(smile,'съешь[<) еще этих фран[<)цузских булок да вы[<)пей чаю'))))
print('Кол-во смайлов: '+ str(len(re.findall(smile,'Lolhkujghe[<)iuruoheourg[<)hjpijpeoiguphh97[<)509-v-0=39=n0'))))
print('Кол-во смайлов: '+ str(len(re.findall(smile,'8-{8-)8-P8-|8-P8<{8<)8<P8<|8<P8-{{8-{)8-{[<)P8-{|8-{P8<{{8<{)8<{P8<{|8<{P;-{;-);-P;-|;-P[<);<{;<);<P;<|;<P;-{{;-{);-{P;-{|;[<)-{P;<{{[<);<{);<{P;<{|;<{PX-{X-)X-PX-|X-PX<{X<)X<PX<|X[<)v[<)<PX-{{X-{)X-{PX-{|X-{PX<{{X<{)X<{PX<{|X<{P:-{:-):-P:-|:-P:<{:<):<P:<|:<P:-{{:-{):-{P:-{|:-{P:<{{:<{):<{P:<{|:<{P'))))
