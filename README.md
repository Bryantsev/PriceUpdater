PriceUpdater
============

Системные требования:
- JRE 1.8 (разработка велась на версии 1.8.0_20)

Описание работы программы

Основные требования к данным (папки указаны для корневой директории программы, а не иконок на рабочем столе):
файлы прайсов для обработки должны располагаться в папке data в формате xlsx или xls:
- data\source.xlsx - прайс, который требуется обновить. Обязательный файл.
- data\*.xlsx - прайсы поставщиков типа 1. Парсинг данных файлов настраивается с помощью файлов настроек по одному на каждый тип поставщика/файла с простой структурой в виде таблицы. На данный момент добавлены настройки для 2х поставщиков, по аналогии м.б. созданы и для других.

Пути к файлам прайсов зашиты в параметрах в строке запуска файла start.bat. Имена и путь к файлам можно изменить на любые другие (возможны проблемы с русскими буквами, поэтому надежнее будет английский), но имеет смысл оставить тот, что есть. Порядок параметров не имеет значение, если вдруг захочется поменять строку вызова приложения. Параметры предваряются 2мя символами дефиса/минуса.
Пример из файла start.bat
--source_file=data\source.xlsx --suppliers=supplier1,data\supplier1.xlsx,supplier2,data\supplier2.xlsx
Оба параметра обязательны. Если будут добавлены настройки для нового поставщика, то необходимо в командную строку start.bat к параметру suppliers добавить через запятую еще 2 значения, например, ",supplier3,<путь к прайсу 3го поставщика>". При этом в папке config д.б. файл supplier3.ini с настройками парсинга данных. Значение файла настроек понятно из его содержания.

Запуск программы
Программа запускается с помощью файла start.bat.

Результат выполнения программы:
в папке results в подпапке с именем годмесяцдень (yyyymmdd) сохраняются:
- обновленный прайс с именем исходного файла с добавлением перед расширением даты и времени в формате "ddMMyyyy_HHmmss".
- лог изменений прайса с именем файла source_changes_<имя/тип поставщика>_<ddMMyyyy_HHmmss>.txt с добавлением перед расширением даты и времени в формате "ddMMyyyy_HHmmss".
