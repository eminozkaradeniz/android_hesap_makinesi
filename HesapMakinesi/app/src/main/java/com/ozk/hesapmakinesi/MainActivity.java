package com.ozk.hesapmakinesi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    public static final int MAX_INPUT_LEN = 16;
    private int inputLen;
    private EditText inputView;
    private static Stack<Character> input;
    private String numbers;
    private String signs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        numbers = "0123456789";
        signs = "+-x/";
        inputLen = 1;
        inputView = findViewById(R.id.tvInput);
        input = new Stack<>();
        input.push('0');
    }

    private void updateInput(char c) {
        //Bu metotta kullanıcının girdiği karakterin o anki girdiye olan uygunluğu kontrol ediliyor.

        String lastChar = input.get(inputLen - 1).toString();

        if (c == '=' && numbers.contains(lastChar)) {
            calculate();
        }

        if (inputLen == MAX_INPUT_LEN) {
            Toast.makeText(this, "Maksimum girdi boyutu aşıldı!", Toast.LENGTH_SHORT).show();
            return;
        }

        /* Hiç karakter girişi yapılmadıysa ya da ekran temizlendiyse kullanıcıya 0 sayısı gösteriliyor.
        Bu durumdayken sayı girişi yapıldıysa 0 sayısı girdiden çıkarılıyor. */
        if (input.get(0) == '0' && inputLen == 1 && numbers.contains(Character.toString(c))) {
            input.pop();
            inputLen = 0;
        }

        /* Girilen karakter nokta olduğu durumda bir önceki karakterin nokta ya da işlem olmaması
        durumu kontrol ediliyor. */
        if (c == '.' && (lastChar.equals(".") || signs.contains(lastChar)))
            return;

        /* Girilen karakter işlem karakteri ise bir önceki karakterin işlem veya nokta olmaması durumu
        kontrol ediliyor. */
        if (signs.contains(Character.toString(c)) && ((signs.contains(lastChar) ||
                lastChar.equals(".")))) {
            return;
        }

        /* Girilen karakterin nokta olduğu durumda, bu karakterden önce sayı ve/veya işlem bulunması
        durumu kontrol ediliyor.  Örnek: 33+20.203. geçerli olmayan durumun girilmesini engelliyor.*/
        if (c == '.') {
            String inputS = inputToString();
            for (int i = inputS.length() - 2; i >= 0; i--) {
                if (inputS.charAt(i) == '.') return;
                else if (signs.contains(Character.toString(inputS.charAt(i)))) break;
            }
        }

        // Gerekli kontroller tamamlandıktan sonra, karakter girme işlemi bitiyor.
        inputLen++;
        input.push(c);
        inputView.setText(inputToString());
    }

    private void resetInput() {
        input.clear();
        input.push('0');
        inputLen = 1;
        inputView.setText(inputToString());
    }

    private String inputToString() {
        StringBuilder s = new StringBuilder();
        for (Character c : input)
            s.append(c);
        return s.toString();
    }

    private void negate() {
        // Girilen son sayının işaretini değiştiriyor. (-1 ile çarpıyor)

        String inputS = inputToString();
        if (inputS.equals("0")) return;
        int i;
        for (i = inputS.length() - 1; i >= 0; i--) {
            char c = inputS.charAt(i);
            if (signs.contains(Character.toString(c))) {
                if (c == '+') {
                    input.set(i, '-');
                    break;
                } else if (c == '-') {
                    input.set(i, '+');
                    break;
                } else
                    input.add(i + 1, '-');
            }
        }
        if (i == -1) input.add(0, '-');
        inputView.setText(inputToString());
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void calculate() {

        if (!numbers.contains(input.get(inputLen - 1).toString())) {
            Toast.makeText(this, "Invalid input!", Toast.LENGTH_SHORT).show();
            return;
        }

        String toCalc = inputToString();
        LinkedList<Character> ops = new LinkedList<>(); //İşlemlerin tutulacağı liste
        for (int i = 1; i < input.size(); i++) {
            char c = input.get(i);
            if (signs.contains(Character.toString(c))) {
                /* Eğer bir sayının işareti negatif ise, regex ile negatif sayı olarak okunduğundan
                dolayı, bu kısımdaki işlem toplama işlemi olarak alınıyor. */
                if (c == '-' && !signs.contains(Character.toString(input.get(i - 1))))
                    ops.add('+');
                else
                    ops.add(c);
            }
        }

        LinkedList<Double> numbers = new LinkedList<>(); //Sayıların tutulacağı liste
        // Bir String deki tüm gerçek sayıları bulmayı sağlayan regular expression
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        Matcher matcher = pattern.matcher(toCalc);
        while (matcher.find()) {
            numbers.add(Double.valueOf(matcher.group()));
        }

        //İşlem önceliğinden dolayı ilk çarpma ve bölme işlemleri yapılıyor
        int n = ops.size();
        double d;
        for (int i = 0; i < n; i++) {
            char op = ops.get(i);
            if (op == 'x') {
                d = numbers.get(i) * numbers.get(i + 1);
                ops.remove(i);
                numbers.set(i, d);
                numbers.remove(i + 1);
                i--;
                n--;
            } else if (op == '/') {
                d = numbers.get(i) / numbers.get(i + 1);
                ops.remove(i);
                numbers.set(i, d);
                numbers.remove(i + 1);
                i--;
                n--;
            }
        }

        n = ops.size();
        for (int i = 0; i < n; i++) {
            char op = ops.get(i);
            if (op == '+') {
                d = numbers.get(i) + numbers.get(i + 1);
                ops.remove(i);
                numbers.set(i, d);
                numbers.remove(i + 1);
                i--;
                n--;
            }
        }
        input.clear();
        input.push('0');
        inputLen = 1;

        //Sonuç: numbers listesindeki bulunan tek değer
        String result = Double.toString((Math.round(numbers.get(0) * Math.pow(10, 4)) / Math.pow(10, 4)));
        if (result.endsWith(".0")) result = result.substring(0, result.length() - 2);
        inputView.setText(result);

    }

    @SuppressLint("NonConstantResourceId")
    public void buttonClick(View view) {
        Button b = (Button) view;
        int id = b.getId();

        switch (id) {
            case R.id.buttonClear:
                resetInput();
                break;
            case R.id.buttonBs:
                if (inputLen == 1) {
                    resetInput();
                } else {
                    input.pop();
                    inputLen--;
                    inputView.setText(inputToString());
                }
                break;
            case R.id.buttonE:
                calculate();
                break;
            case R.id.buttonAS:
                negate();
                break;
            default:
                updateInput(b.getText().charAt(0));
                break;
        }

    }

}