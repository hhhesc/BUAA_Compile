import filecmp
import os

dir0 = ['2021_B']
for dir in dir0:
    for i in range(1, 31):
        testfile = os.path.join(dir, 'testfile' + str(i) + '.txt')
        input = os.path.join(dir, 'input' + str(i) + '.txt')
        output = os.path.join(dir, 'output' + str(i) + '.txt')
        os.system(r'copy ' + testfile + '..\testfile.txt')
        os.system(r'copy ' + input + '..\input.txt')
        os.system(r'type ..\input.txt | java -jar ..\mars.jar ic nc ..\mips.txt > ..\output.txt')

        ac = filecmp.cmp(output, '..\output.txt')
        if not ac:
            print('error in ' + testfile)
        else:
            print("ac in" + testfile)
