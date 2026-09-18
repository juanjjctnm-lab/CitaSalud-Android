package pe.citasalud.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int PRIMARY = Color.rgb(15, 118, 110);
    private static final int PRIMARY_DARK = Color.rgb(17, 94, 89);
    private static final int BG = Color.rgb(247, 250, 249);
    private static final int TEXT = Color.rgb(23, 49, 47);
    private static final int MUTED = Color.rgb(100, 116, 139);
    private static final int BORDER = Color.rgb(226, 232, 240);
    private static final int WHITE = Color.WHITE;

    private SharedPreferences session;
    private Db db;
    private LinearLayout content;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = getSharedPreferences("citasalud_session", MODE_PRIVATE);
        db = new Db(this);

        if (session.getBoolean("logged", false)) {
            showApp();
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(BG);

        LinearLayout root = column(24);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(42), dp(24), dp(30));
        sv.addView(root, matchWrap());

        TextView logo = title("+", 52);
        logo.setTextColor(PRIMARY);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo, new LinearLayout.LayoutParams(dp(72), dp(72)));

        TextView title = title("CitaSalud", 30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = text("Reserva tus citas médicas desde tu celular", 15, MUTED);
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle);

        EditText email = input("Correo electrónico");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setText(session.getString("email", "paciente@citasalud.pe"));
        root.addView(email);
        marginTop(email, 30);

        EditText pass = input("Contraseña");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setText(session.getString("password", "123456"));
        root.addView(pass);
        marginTop(pass, 12);

        Button login = primaryButton("Iniciar sesión");
        root.addView(login);
        marginTop(login, 20);

        Button register = outlineButton("Crear cuenta");
        root.addView(register);
        marginTop(register, 12);

        TextView demo = text("Demo: paciente@citasalud.pe / 123456", 12, MUTED);
        demo.setGravity(Gravity.CENTER);
        root.addView(demo);
        marginTop(demo, 16);

        login.setOnClickListener(v -> {
            String savedEmail = session.getString("email", "paciente@citasalud.pe");
            String savedPass = session.getString("password", "123456");
            if (savedEmail.equalsIgnoreCase(email.getText().toString().trim())
                    && savedPass.equals(pass.getText().toString())) {
                session.edit().putBoolean("logged", true).apply();
                showApp();
            } else {
                Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            }
        });

        register.setOnClickListener(v -> showRegister());
        setContentView(sv);
    }

    private void showRegister() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(BG);
        LinearLayout root = column(0);
        root.setPadding(dp(22), dp(28), dp(22), dp(30));
        sv.addView(root, matchWrap());

        root.addView(title("Crear cuenta", 28));
        root.addView(text("Completa tus datos para reservar citas.", 14, MUTED));

        EditText name = input("Nombres y apellidos");
        root.addView(name); marginTop(name, 24);
        EditText dni = input("DNI");
        dni.setInputType(InputType.TYPE_CLASS_NUMBER);
        root.addView(dni); marginTop(dni, 12);
        EditText phone = input("Teléfono");
        phone.setInputType(InputType.TYPE_CLASS_PHONE);
        root.addView(phone); marginTop(phone, 12);
        EditText email = input("Correo electrónico");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        root.addView(email); marginTop(email, 12);
        EditText pass = input("Contraseña (mínimo 6 caracteres)");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        root.addView(pass); marginTop(pass, 12);

        Button save = primaryButton("Registrarme");
        root.addView(save); marginTop(save, 20);
        Button back = outlineButton("Volver");
        root.addView(back); marginTop(back, 10);

        save.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            String d = dni.getText().toString().trim();
            String p = phone.getText().toString().trim();
            String e = email.getText().toString().trim();
            String pw = pass.getText().toString();

            if (n.length() < 4 || d.length() != 8 || p.length() < 7 || !e.contains("@") || pw.length() < 6) {
                Toast.makeText(this, "Revisa los datos ingresados", Toast.LENGTH_SHORT).show();
                return;
            }

            session.edit()
                    .putString("name", n)
                    .putString("dni", d)
                    .putString("phone", p)
                    .putString("email", e)
                    .putString("password", pw)
                    .apply();

            Toast.makeText(this, "Cuenta creada. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show();
            showLogin();
        });

        back.setOnClickListener(v -> showLogin());
        setContentView(sv);
    }

    private void showApp() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(BG);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        page.addView(content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        page.addView(buildBottomNav(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));

        setContentView(page);
        showHome();
    }

    private View buildBottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(WHITE);
        nav.setPadding(dp(4), dp(4), dp(4), dp(4));

        nav.addView(navButton("Inicio", v -> showHome()), weight());
        nav.addView(navButton("Reservar", v -> showBooking()), weight());
        nav.addView(navButton("Mis citas", v -> showAppointments()), weight());
        nav.addView(navButton("Perfil", v -> showProfile()), weight());
        return nav;
    }

    private void showHome() {
        LinearLayout b = screenBody();
        String name = session.getString("name", "Juan Pérez");

        b.addView(text("Hola, " + name, 15, MUTED));
        b.addView(title("CitaSalud", 28));

        TextView hero = title("¿Necesitas una cita médica?", 21);
        hero.setTextColor(PRIMARY_DARK);
        hero.setPadding(dp(18), dp(22), dp(18), dp(22));
        hero.setBackground(bg(0xFFE1F4F1, 20));
        b.addView(hero);
        marginTop(hero, 22);

        TextView desc = text("Reserva una cita, revisa su estado y recibe avisos del centro de salud.", 15, MUTED);
        b.addView(desc);
        marginTop(desc, 12);

        Button reserve = primaryButton("Reservar una cita");
        b.addView(reserve);
        marginTop(reserve, 18);
        reserve.setOnClickListener(v -> showBooking());

        Button notes = outlineButton("Ver notificaciones");
        b.addView(notes);
        marginTop(notes, 10);
        notes.setOnClickListener(v -> showNotifications());

        b.addView(sectionTitle("Resumen"));

        int total = db.listAppointments().size();
        TextView stats = text(
                "Citas registradas: " + total +
                        "\nEstado de la app: activa" +
                        "\nCompatibilidad mínima: Android 5.0",
                15, TEXT
        );
        stats.setPadding(dp(16), dp(16), dp(16), dp(16));
        stats.setBackground(outline(WHITE, BORDER, 16));
        b.addView(stats);
    }

    private void showBooking() {
        LinearLayout b = screenBody();
        b.addView(title("Reservar cita", 27));
        b.addView(text("Selecciona especialidad, médico, fecha y horario.", 14, MUTED));

        String[] specs = {"Medicina General", "Pediatría", "Odontología", "Obstetricia", "Psicología"};
        Spinner specialty = new Spinner(this);
        specialty.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, specs));
        b.addView(specialty);
        marginTop(specialty, 18);

        String[] doctors = {"Dr. Carlos Torres", "Dra. Ana Ramos", "Dra. Lucía Mendoza"};
        Spinner doctor = new Spinner(this);
        doctor.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, doctors));
        b.addView(doctor);
        marginTop(doctor, 8);

        Button date = outlineButton("Seleccionar fecha");
        b.addView(date);
        marginTop(date, 12);

        date.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, y, m, d) -> {
                        selectedDate = String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y);
                        date.setText("Fecha: " + selectedDate);
                    },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        b.addView(sectionTitle("Hora disponible"));

        String[] times = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00"};
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);

        for (String tm : times) {
            Button x = new Button(this);
            x.setText(tm);
            x.setAllCaps(false);
            x.setBackground(outline(WHITE, BORDER, 12));

            GridLayout.LayoutParams gp = new GridLayout.LayoutParams();
            gp.width = 0;
            gp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            gp.setMargins(dp(3), dp(3), dp(3), dp(3));
            x.setLayoutParams(gp);

            x.setOnClickListener(v -> {
                selectedTime = tm;
                Toast.makeText(this, "Hora seleccionada: " + tm, Toast.LENGTH_SHORT).show();
            });
            grid.addView(x);
        }
        b.addView(grid);

        EditText reason = input("Motivo de consulta");
        reason.setSingleLine(false);
        reason.setMinLines(3);
        reason.setGravity(Gravity.TOP);
        b.addView(reason);
        marginTop(reason, 16);

        Button send = primaryButton("Solicitar cita");
        b.addView(send);
        marginTop(send, 18);

        send.setOnClickListener(v -> {
            String r = reason.getText().toString().trim();
            if (selectedDate.isEmpty() || selectedTime.isEmpty() || r.length() < 4) {
                Toast.makeText(this, "Completa fecha, hora y motivo", Toast.LENGTH_SHORT).show();
                return;
            }

            db.addAppointment(
                    specs[specialty.getSelectedItemPosition()],
                    doctors[doctor.getSelectedItemPosition()],
                    selectedDate,
                    selectedTime,
                    r
            );

            Toast.makeText(this, "Solicitud registrada como Pendiente", Toast.LENGTH_LONG).show();
            selectedDate = "";
            selectedTime = "";
            showAppointments();
        });
    }

    private void showAppointments() {
        LinearLayout b = screenBody();
        b.addView(title("Mis citas", 27));
        b.addView(text("Consulta y administra tus reservas.", 14, MUTED));

        List<Appointment> list = db.listAppointments();

        if (list.isEmpty()) {
            TextView empty = text("Todavía no tienes citas. Pulsa Reservar para crear la primera.", 15, MUTED);
            b.addView(empty);
            marginTop(empty, 22);
            return;
        }

        for (Appointment a : list) {
            LinearLayout card = column(0);
            card.setPadding(dp(16), dp(15), dp(16), dp(15));
            card.setBackground(outline(WHITE, BORDER, 16));

            card.addView(title(a.doctor, 17));
            card.addView(text(a.specialty, 14, MUTED));
            card.addView(text(a.date + " · " + a.time, 14, TEXT));

            int statusColor = "Cancelada".equals(a.status) ? 0xFFB91C1C : PRIMARY_DARK;
            TextView st = text("Estado: " + a.status, 14, statusColor);
            st.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            card.addView(st);

            if (!"Cancelada".equals(a.status)) {
                Button cancel = outlineButton("Cancelar cita");
                card.addView(cancel);
                marginTop(cancel, 10);
                cancel.setOnClickListener(v -> {
                    db.cancelAppointment(a.id);
                    showAppointments();
                });
            }

            b.addView(card);
            marginTop(card, 12);
        }
    }

    private void showNotifications() {
        LinearLayout b = screenBody();
        b.addView(title("Notificaciones", 27));
        b.addView(text("Avisos y confirmaciones del establecimiento.", 14, MUTED));

        for (String[] n : db.listNotifications()) {
            LinearLayout card = column(0);
            card.setPadding(dp(16), dp(14), dp(16), dp(14));
            card.setBackground(outline(WHITE, BORDER, 16));
            card.addView(title(n[0], 16));
            card.addView(text(n[1], 14, TEXT));
            card.addView(text(n[2], 12, MUTED));
            b.addView(card);
            marginTop(card, 12);
        }
    }

    private void showProfile() {
        LinearLayout b = screenBody();
        b.addView(title("Mi perfil", 27));
        b.addView(text(session.getString("name", "Juan Pérez"), 20, TEXT));
        b.addView(text("Paciente", 14, MUTED));

        String profile =
                "DNI: " + session.getString("dni", "12345678") +
                "\nTeléfono: " + session.getString("phone", "999 999 999") +
                "\nCorreo: " + session.getString("email", "paciente@citasalud.pe");

        TextView data = text(profile, 15, TEXT);
        data.setPadding(dp(16), dp(16), dp(16), dp(16));
        data.setBackground(outline(WHITE, BORDER, 16));
        b.addView(data);
        marginTop(data, 20);

        Button logout = outlineButton("Cerrar sesión");
        b.addView(logout);
        marginTop(logout, 18);
        logout.setOnClickListener(v -> {
            session.edit().putBoolean("logged", false).apply();
            showLogin();
        });
    }

    private LinearLayout screenBody() {
        content.removeAllViews();

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);

        LinearLayout body = column(0);
        body.setPadding(dp(20), dp(20), dp(20), dp(24));

        sv.addView(body, matchWrap());
        content.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return body;
    }

    private TextView sectionTitle(String s) {
        TextView t = title(s, 18);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        p.topMargin = dp(24);
        p.bottomMargin = dp(10);
        t.setLayoutParams(p);
        return t;
    }

    private Button navButton(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(12);
        b.setTextColor(PRIMARY_DARK);
        b.setBackgroundColor(WHITE);
        b.setOnClickListener(listener);
        return b;
    }

    private LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
    }

    private LinearLayout column(int top) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        if (top > 0) l.setPadding(0, dp(top), 0, 0);
        return l;
    }

    private TextView title(String s, int sp) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(TEXT);
        v.setTextSize(sp);
        v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private TextView text(String s, int sp, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(color);
        v.setTextSize(sp);
        return v;
    }

    private EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(16);
        e.setTextColor(TEXT);
        e.setHintTextColor(MUTED);
        e.setPadding(dp(14), 0, dp(14), 0);
        e.setBackground(outline(WHITE, BORDER, 14));
        e.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        return e;
    }

    private Button primaryButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(bg(PRIMARY, 14));
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        return b;
    }

    private Button outlineButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(PRIMARY_DARK);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(outline(WHITE, BORDER, 14));
        b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));
        return b;
    }

    private GradientDrawable bg(int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    private GradientDrawable outline(int fill, int stroke, int radiusDp) {
        GradientDrawable g = bg(fill, radiusDp);
        g.setStroke(dp(1), stroke);
        return g;
    }

    private void marginTop(View v, int valueDp) {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        if (p instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) p).topMargin = dp(valueDp);
            v.setLayoutParams(p);
        }
    }

    private ViewGroup.LayoutParams matchWrap() {
        return new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static class Appointment {
        long id;
        String specialty;
        String doctor;
        String date;
        String time;
        String reason;
        String status;

        Appointment(long id, String specialty, String doctor, String date, String time, String reason, String status) {
            this.id = id;
            this.specialty = specialty;
            this.doctor = doctor;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.status = status;
        }
    }

    private static class Db extends SQLiteOpenHelper {
        private static final String DB_NAME = "citasalud.db";
        private static final int DB_VERSION = 1;

        Db(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE appointments(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "specialty TEXT NOT NULL," +
                            "doctor TEXT NOT NULL," +
                            "date TEXT NOT NULL," +
                            "time TEXT NOT NULL," +
                            "reason TEXT NOT NULL," +
                            "status TEXT NOT NULL)"
            );

            database.execSQL(
                    "CREATE TABLE notifications(" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "title TEXT NOT NULL," +
                            "body TEXT NOT NULL," +
                            "created_at TEXT NOT NULL)"
            );

            ContentValues n = new ContentValues();
            n.put("title", "Bienvenido a CitaSalud");
            n.put("body", "Desde aquí podrás reservar y consultar tus citas médicas.");
            n.put("created_at", "Hoy");
            database.insert("notifications", null, n);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        long addAppointment(String specialty, String doctor, String date, String time, String reason) {
            ContentValues v = new ContentValues();
            v.put("specialty", specialty);
            v.put("doctor", doctor);
            v.put("date", date);
            v.put("time", time);
            v.put("reason", reason);
            v.put("status", "Pendiente");

            long id = getWritableDatabase().insert("appointments", null, v);
            addNotification("Solicitud enviada", "Tu cita para " + date + " a las " + time + " quedó pendiente de confirmación.");
            return id;
        }

        List<Appointment> listAppointments() {
            ArrayList<Appointment> out = new ArrayList<>();
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT id,specialty,doctor,date,time,reason,status FROM appointments ORDER BY id DESC",
                    null
            );

            try {
                while (c.moveToNext()) {
                    out.add(new Appointment(
                            c.getLong(0),
                            c.getString(1),
                            c.getString(2),
                            c.getString(3),
                            c.getString(4),
                            c.getString(5),
                            c.getString(6)
                    ));
                }
            } finally {
                c.close();
            }

            return out;
        }

        void cancelAppointment(long id) {
            ContentValues v = new ContentValues();
            v.put("status", "Cancelada");
            getWritableDatabase().update("appointments", v, "id=?", new String[]{String.valueOf(id)});
            addNotification("Cita cancelada", "La cita seleccionada fue cancelada desde tu aplicación.");
        }

        void addNotification(String title, String body) {
            ContentValues n = new ContentValues();
            n.put("title", title);
            n.put("body", body);
            n.put("created_at", "Ahora");
            getWritableDatabase().insert("notifications", null, n);
        }

        List<String[]> listNotifications() {
            ArrayList<String[]> out = new ArrayList<>();
            Cursor c = getReadableDatabase().rawQuery(
                    "SELECT title,body,created_at FROM notifications ORDER BY id DESC",
                    null
            );

            try {
                while (c.moveToNext()) {
                    out.add(new String[]{c.getString(0), c.getString(1), c.getString(2)});
                }
            } finally {
                c.close();
            }

            return out;
        }
    }
}
