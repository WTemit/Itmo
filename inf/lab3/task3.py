import re

def task3(text):
    words = re.findall(r'\b[a-zA-Zа-яА-Я0-9]+\b', text)
    bykvi = 'АаЕеЁёИиОоУуЫыЭэЮюЯя'
    one_glasn_words = []
    for word in words:
        # Считаем количество уникальных гласных в слове
        unique_vowels = set(re.findall(f'[{bykvi}]', word))
        
        # Проверяем, если в слове ровно одна гласная
        if len(unique_vowels) == 1:
            one_glasn_words.append(word)
    
    # Сортируем и выводим уникальные слова
    print(sorted(set(one_glasn_words), key=lambda t: (len(t), t)))

# Пример текста
t1 = 'Классное слово – обороноспособность, которое должно идти после слов: трава и молоко.'
t2 = "В окне молоко, а трава зеленая, в стоге сена"
t3 = 'Мама мыла раму, а папа читал газету'
t4 = 'Слон на салат, как шаль, пылит в даль'
t5 = "Кот топтался вдоль мостов, шёл вглубь дворов"

# Запускаем функцию
task3(t1)
task3(t2)
task3(t3)
task3(t4)
task3(t5)
