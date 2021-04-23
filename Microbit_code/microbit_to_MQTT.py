from microbit import *

uart.init(baudrate = 115200)
zero = Image("00000:"
             "00000:"
             "00000:"
             "00000:"
             "00000")
one = Image("99999:"
            "99999:"
            "99999:"
            "99999:"
            "99999")

while True:
    if button_a.is_pressed():
        uart.write("#0!")
        display.show(zero)
        sleep(200)
    elif button_b.is_pressed():
        uart.write("#1!")
        display.show(one)
        sleep(200)
    if uart.any():
        msg = uart.read()
        msg = str(msg, 'UTF-8')
        if msg == '0':
            display.show(zero)
        elif msg == '1':
            display.show(one)
        else:
            display.scroll(msg)