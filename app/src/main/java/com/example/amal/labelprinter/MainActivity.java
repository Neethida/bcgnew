package com.example.amal.labelprinter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Locale;
import  java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static com.example.amal.labelprinter.R.id.itemNumber;
import static com.example.amal.labelprinter.R.id.text;

public class MainActivity extends AppCompatActivity {
    ImageView previewImage;
    Bitmap barcode;
    EditText dateMy;
    EditText itemNumber;
    EditText caseCount;
    EditText skidNumber;
    EditText parallelizer;
    TextView itemId;
    TextView caseCountId;
    TextView skidCountId;
    TextView prodDateId;
    TextView palletizersinitialId;
    LinearLayout linearLayout1;
    SimpleDateFormat dateF = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewImage = (ImageView)findViewById(R.id.previewImg);
        linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
        linearLayout1.setVisibility(View.INVISIBLE);

        //linearLayout1.Visibility= ViewStates.Invisible;
        itemNumber =(EditText)findViewById(R.id.itemNumber);
        caseCount = (EditText)findViewById(R.id.caseCount);
        skidNumber = (EditText)findViewById(R.id.skidNumber);
        parallelizer = (EditText)findViewById(R.id.parallelizer);
        dateMy = (EditText)findViewById(R.id.dateMy);

        //table textview
        itemId = (TextView)findViewById(R.id.itemId);
        caseCountId = (TextView)findViewById(R.id.caseCountId);
        skidCountId = (TextView)findViewById(R.id.skidCountId);
        prodDateId = (TextView)findViewById(R.id.prodDateId);
        palletizersinitialId = (TextView)findViewById(R.id.palletizersinitialId);
        String dateCurrent = dateF.format(Calendar.getInstance().getTime());
        dateMy.setText(dateCurrent);

        dateMy.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar myCurrentDate = Calendar.getInstance();
                int mYear = myCurrentDate.get(Calendar.YEAR);
                int mMonth = myCurrentDate.get(Calendar.MONTH);
                int mDay = myCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker;

                mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        Date date = new GregorianCalendar(selectedyear, selectedmonth, selectedday).getTime();
                        dateMy.setText(dateF.format(date));
                    }
                }, mYear, mMonth, mDay);

                mDatePicker.setTitle("Select Date");
                mDatePicker.show();
            }
        });
        Button  previewBtn = (Button)findViewById(R.id.previewBtn);
        previewBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {


                GenerateBarcode(GetBarcodeText());
                linearLayout1.setVisibility(View.VISIBLE);
                previewImage.setImageBitmap(barcode);
            }
        });
        Button  printBtn = (Button)findViewById(R.id.printBtn);
      //  printBtn.setEnabled(false);
        printBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if(itemNumber.getText().toString().length() != 5 || TextUtils.isEmpty(itemNumber.getText().toString())) {
                    itemNumber.setError("Enter Item number");
                    return;
                }
                else if(caseCount.getText().toString().length() != 3 || TextUtils.isEmpty(caseCount.getText().toString())){
                    caseCount.setError("Enter Case count");
                    return;
                }
                else if(skidNumber.getText().toString().length() != 2 || TextUtils.isEmpty(skidNumber.getText().toString())){
                    skidNumber.setError("Enter Skid number");
                    return;
                }
                else if(parallelizer.getText().toString().length() != 2 || TextUtils.isEmpty(parallelizer.getText().toString())){
                    parallelizer.setError("Enter Parallelizer initials");
                    return;
                }
                else if(TextUtils.isEmpty(dateMy.getText().toString())){
                    dateMy.setError("Enter Date");
                    return;
                }
                else{
                    GenerateBarcode(GetBarcodeText());
                    linearLayout1.setVisibility(View.VISIBLE);
                    previewImage.setImageBitmap(barcode);
                    PrintBarcode();
                }
            }
        });



    }

    private  void PrintBarcode()
    {
        PrintManager printManager = (PrintManager)
                this.getSystemService(Context.PRINT_SERVICE);

        GenericPrintAdapter printAdapter =new GenericPrintAdapter(this,linearLayout1);

        printManager.print("MyPrintJob",printAdapter,null).isCompleted();


    }
public class GenericPrintAdapter extends PrintDocumentAdapter{

    View view;
    Context context;
    private int totalPages;
    PrintedPdfDocument document;
    private View mView;

    public GenericPrintAdapter(Context context, View view){

        this.view = view;
        this.context = context;
        totalPages = 3;
        mView = view;
    }
    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

        document = new PrintedPdfDocument(context,newAttributes);
        if(totalPages>0){

            PrintDocumentInfo printDocumentInfo = new PrintDocumentInfo
                    .Builder("MyPrint.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalPages)
                   .build();

            callback.onLayoutFinished(printDocumentInfo, true);
        }
        {
            callback.onLayoutFailed("Page count calculation failed.");
        }

    }
    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {

        for (int i = 0; i < totalPages; i++)
        {
            PrintedPdfDocument.Page page = document.startPage(i);


            Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mView.draw(canvas);
            Rect src = new Rect(0, 0, mView.getWidth(), mView.getHeight());
            Canvas pageCanvas = page.getCanvas();

            float pageWidth = pageCanvas.getWidth();
            float pageHeight = pageCanvas.getHeight();

            float scale = Math.min(pageWidth / src.width(), pageHeight / src.height());
            float left = pageWidth / 2 - src.width() * scale / 2;
            float top = pageHeight / 2 ;
            float right = pageWidth / 2 + src.width() * scale / 2;
            float bottom = pageHeight / 2 + src.height() * scale ;
            RectF dst = new RectF(left, top, right, bottom);

            pageCanvas.drawBitmap(bitmap, src, dst, null);
            document.finishPage(page);
        }

        WritePrintedPdfDoc(destination);

        document.close();

        document = null;

        PageRange[] writtenPages = new PageRange[1];
        writtenPages[0] = new PageRange(1, 3);

        callback.onWriteFinished(pages);

    }
    void WritePrintedPdfDoc(ParcelFileDescriptor destination)
    {
        try {
            java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(destination.getFileDescriptor());
            document.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onFinish() {
        super.onFinish();
        skidNumber.getText().clear();
        skidCountId.setText("");
        palletizersinitialId.setText("");
        parallelizer.getText().clear();
        linearLayout1.setVisibility(View.GONE);


    }

}


    private Bitmap GenerateBarcode(String text){

        itemId.setText(itemNumber.getText());
        caseCountId.setText(caseCount.getText());
        skidCountId.setText(skidNumber.getText());
        prodDateId.setText(dateMy.getText());
        palletizersinitialId.setText(parallelizer.getText());
        // barcode image
        Bitmap bitmap = null;

        try {

            bitmap = encodeAsBitmap(GetBarcodeText(), BarcodeFormat.CODE_128, 1200, 300);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        barcode = bitmap;

        float scale = getResources().getDisplayMetrics().density;
        Canvas canvas = new Canvas(barcode);
        Paint paint =new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(12*scale);
        Rect bounds =new Rect();
        paint.getTextBounds(text,0,text.length(),bounds);
        int x = (barcode.getWidth()-bounds.width())/2;
        int y = (barcode.getHeight()-bounds.height());
        paint.setColor(Color.rgb(250,250,250));
        canvas.drawRect(0,y-10,barcode.getWidth(),barcode.getHeight(),paint);
        paint.setColor(Color.rgb(10,110,80));
        canvas.drawText(text,x,y+bounds.height()-5,paint);
        return barcode;

    }

    private  String GetBarcodeText()
    {
        return dateMy.getText().toString() +" "+itemNumber.getText().toString() +" "+ caseCount.getText().toString() +" "+ skidNumber.getText().toString() ;
    }







    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}


