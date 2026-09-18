package pe.citasalud.app;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
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
    private static final int PRIMARY_DARK = Color.rgb(11, 94, 88);
    private static final int SECONDARY = Color.rgb(24, 181, 164);
    private static final int BG = Color.rgb(244, 248, 250);
    private static final int SURFACE = Color.WHITE;
    private static final int SOFT = Color.rgb(247, 251, 251);
    private static final int TEXT = Color.rgb(22, 54, 52);
    private static final int MUTED = Color.rgb(107, 125, 133);
    private static final int BORDER = Color.rgb(220, 231, 236);
    private static final int SUCCESS = Color.rgb(20, 128, 94);
    private static final int WARNING = Color.rgb(181, 71, 8);
    private static final int DANGER = Color.rgb(180, 35, 24);

    private SharedPreferences session;
    private Db db;
    private LinearLayout content;
    private final Button[] navButtons = new Button[4];
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = getSharedPreferences("citasalud_session", MODE_PRIVATE);
        db = new Db(this);
        if (session.getBoolean("logged", false)) showApp(); else showLogin();
    }

    private void showLogin() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        sv.setBackgroundColor(BG);
        LinearLayout root = column();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        sv.addView(root, matchWrap());

        ImageView logo = logo(92);
        root.addView(logo);

        LinearLayout wordmark = new LinearLayout(this);
        wordmark.setOrientation(LinearLayout.HORIZONTAL);
        wordmark.setGravity(Gravity.CENTER);
        TextView c1 = title("Cita", 30); c1.setTextColor(PRIMARY_DARK);
        TextView c2 = title("Salud", 30); c2.setTextColor(SECONDARY);
        wordmark.addView(c1); wordmark.addView(c2);
        root.addView(wordmark);
        marginTop(wordmark, 12);

        TextView tagline = text("Tu salud, tus citas, en un solo lugar.", 15, MUTED);
        tagline.setGravity(Gravity.CENTER);
        root.addView(tagline);
        marginTop(tagline, 5);

        LinearLayout card = card();
        root.addView(card);
        marginTop(card, 24);
        card.addView(title("Iniciar sesión", 21));
        TextView helper = text("Ingresa como paciente para gestionar tus citas médicas.", 14, MUTED);
        card.addView(helper); marginTop(helper, 6);

        EditText email = input("Correo electrónico");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setText(session.getString("email", "paciente@citasalud.pe"));
        card.addView(email); marginTop(email, 18);

        EditText pass = input("Contraseña");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pass.setText(session.getString("password", "123456"));
        card.addView(pass); marginTop(pass, 12);

        Button login = primaryButton("Entrar ahora");
        card.addView(login); marginTop(login, 18);
        Button register = outlineButton("Crear cuenta");
        card.addView(register); marginTop(register, 12);

        LinearLayout demo = softCard();
        root.addView(demo); marginTop(demo, 16);
        demo.addView(title("Acceso de demostración", 15));
        TextView demoText = text("paciente@citasalud.pe  ·  123456", 13, MUTED);
        demo.addView(demoText); marginTop(demoText, 5);

        login.setOnClickListener(v -> {
            String savedEmail = session.getString("email", "paciente@citasalud.pe");
            String savedPass = session.getString("password", "123456");
            if (savedEmail.equalsIgnoreCase(email.getText().toString().trim()) && savedPass.equals(pass.getText().toString())) {
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
        LinearLayout root = column();
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(24), dp(22), dp(26));
        sv.addView(root, matchWrap());

        root.addView(logo(74));
        TextView head = title("Crear cuenta", 27); head.setGravity(Gravity.CENTER);
        root.addView(head); marginTop(head, 10);
        TextView sub = text("Registra tus datos para empezar a reservar citas.", 14, MUTED); sub.setGravity(Gravity.CENTER);
        root.addView(sub); marginTop(sub, 5);

        LinearLayout form = card();
        root.addView(form); marginTop(form, 20);

        EditText name = input("Nombres y apellidos"); form.addView(name);
        EditText dni = input("DNI"); dni.setInputType(InputType.TYPE_CLASS_NUMBER); form.addView(dni); marginTop(dni, 12);
        EditText phone = input("Teléfono"); phone.setInputType(InputType.TYPE_CLASS_PHONE); form.addView(phone); marginTop(phone, 12);
        EditText email = input("Correo electrónico"); email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS); form.addView(email); marginTop(email, 12);
        EditText pass = input("Contraseña (mínimo 6 caracteres)"); pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); form.addView(pass); marginTop(pass, 12);

        Button save = primaryButton("Registrarme"); form.addView(save); marginTop(save, 18);
        Button back = outlineButton("Volver al inicio de sesión"); form.addView(back); marginTop(back, 12);

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
            session.edit().putString("name", n).putString("dni", d).putString("phone", p).putString("email", e).putString("password", pw).apply();
            Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_LONG).show();
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
        page.addView(buildBottomNav(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(78)));
        setContentView(page);
        showHome();
    }

    private View buildBottomNav() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.HORIZONTAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(12), dp(8), dp(12), dp(10));
        outer.setBackground(outline(SURFACE, BORDER, 24));
        navButtons[0] = navButton("Inicio", v -> showHome());
        navButtons[1] = navButton("Reservar", v -> showBooking());
        navButtons[2] = navButton("Mis citas", v -> showAppointments());
        navButtons[3] = navButton("Perfil", v -> showProfile());
        for (Button b : navButtons) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1f);
            lp.setMargins(dp(3), 0, dp(3), 0);
            outer.addView(b, lp);
        }
        return outer;
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setOnClickListener(listener);
        styleNav(b, false);
        return b;
    }

    private void styleNav(Button b, boolean active) {
        b.setTypeface(Typeface.DEFAULT, active ? Typeface.BOLD : Typeface.NORMAL);
        b.setTextColor(active ? Color.WHITE : MUTED);
        b.setBackground(active ? gradient(PRIMARY, SECONDARY, 18) : bg(Color.TRANSPARENT, 18));
    }

    private void selectTab(int index) {
        for (int i = 0; i < navButtons.length; i++) styleNav(navButtons[i], i == index);
    }

    private void showHome() {
        selectTab(0);
        LinearLayout b = body(screen());
        String name = session.getString("name", "Juan Pérez");
        addHeader(b, "Hola, " + firstName(name), "Bienvenido a CitaSalud");

        LinearLayout hero = card();
        hero.setBackground(gradient(PRIMARY_DARK, SECONDARY, 26));
        b.addView(hero); marginTop(hero, 20);
        TextView badge = text("CITAS MÉDICAS", 12, Color.rgb(211, 255, 248)); badge.setTypeface(Typeface.DEFAULT_BOLD);
        hero.addView(badge);
        TextView h = title("Reserva tu próxima cita en segundos", 22); h.setTextColor(Color.WHITE);
        hero.addView(h); marginTop(h, 8);
        TextView p = text("Selecciona especialidad, médico, fecha y horario desde tu celular.", 14, Color.rgb(230, 255, 251));
        hero.addView(p); marginTop(p, 7);
        Button reserve = outlineButton("Nueva cita"); reserve.setBackground(bg(Color.rgb(246, 255, 253), 18)); reserve.setTextColor(PRIMARY_DARK);
        hero.addView(reserve); marginTop(reserve, 16); reserve.setOnClickListener(v -> showBooking());

        b.addView(sectionTitle("Resumen rápido"));
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        b.addView(row);
        LinearLayout s1 = statCard("Citas", String.valueOf(db.listAppointments().size()), "Registradas");
        LinearLayout s2 = statCard("Estado", "Activo", "Sistema disponible");
        row.addView(s1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams s2lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); s2lp.leftMargin = dp(10);
        row.addView(s2, s2lp);

        b.addView(sectionTitle("Accesos rápidos"));
        LinearLayout quick = card(); b.addView(quick);
        Button notif = outlineButton("Ver notificaciones"); quick.addView(notif); notif.setOnClickListener(v -> showNotifications());
        Button cita = outlineButton("Ver mis citas"); quick.addView(cita); marginTop(cita, 10); cita.setOnClickListener(v -> showAppointments());
    }

    private void showBooking() {
        selectTab(1);
        selectedDate = ""; selectedTime = "";
        LinearLayout b = body(screen());
        addHeader(b, "Reservar cita", "Completa los datos de tu atención");

        LinearLayout form = card(); b.addView(form); marginTop(form, 18);
        String[] specs = {"Medicina General", "Pediatría", "Odontología", "Obstetricia", "Psicología"};
        String[] doctors = {"Dr. Carlos Torres", "Dra. Ana Ramos", "Dra. Lucía Mendoza"};

        form.addView(label("Especialidad"));
        Spinner specialty = spinner(specs); form.addView(specialty); marginTop(specialty, 6);
        TextView l2 = label("Médico"); form.addView(l2); marginTop(l2, 14);
        Spinner doctor = spinner(doctors); form.addView(doctor); marginTop(doctor, 6);

        Button date = outlineButton("Seleccionar fecha"); form.addView(date); marginTop(date, 14);
        date.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dlg = new DatePickerDialog(this, (view, y, m, d) -> {
                selectedDate = String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y);
                date.setText("Fecha: " + selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
            dlg.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dlg.show();
        });

        TextView hourLabel = label("Hora disponible"); form.addView(hourLabel); marginTop(hourLabel, 14);
        GridLayout grid = new GridLayout(this); grid.setColumnCount(3);
        String[] times = {"08:00","08:30","09:00","09:30","10:00","10:30","11:00","11:30","12:00"};
        List<Button> timeButtons = new ArrayList<>();
        for (String tm : times) {
            Button x = chipButton(tm);
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams(); gp.width = 0; gp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); gp.setMargins(dp(4), dp(4), dp(4), dp(4)); x.setLayoutParams(gp);
            x.setOnClickListener(v -> {
                selectedTime = tm;
                for (Button other : timeButtons) { other.setTextColor(PRIMARY_DARK); other.setBackground(outline(SURFACE, BORDER, 16)); }
                x.setTextColor(Color.WHITE); x.setBackground(gradient(PRIMARY, SECONDARY, 16));
            });
            timeButtons.add(x); grid.addView(x);
        }
        form.addView(grid); marginTop(grid, 5);

        EditText reason = input("Motivo de consulta"); reason.setSingleLine(false); reason.setMinLines(4); reason.setGravity(Gravity.TOP); reason.setPadding(dp(16), dp(14), dp(16), dp(14));
        form.addView(reason); marginTop(reason, 16);
        Button send = primaryButton("Solicitar cita"); form.addView(send); marginTop(send, 18);
        send.setOnClickListener(v -> {
            String r = reason.getText().toString().trim();
            if (selectedDate.isEmpty() || selectedTime.isEmpty() || r.length() < 4) {
                Toast.makeText(this, "Completa fecha, hora y motivo", Toast.LENGTH_SHORT).show(); return;
            }
            db.addAppointment(specs[specialty.getSelectedItemPosition()], doctors[doctor.getSelectedItemPosition()], selectedDate, selectedTime, r);
            Toast.makeText(this, "Solicitud registrada como Pendiente", Toast.LENGTH_LONG).show();
            showAppointments();
        });
    }

    private void showAppointments() {
        selectTab(2);
        LinearLayout b = body(screen());
        addHeader(b, "Mis citas", "Consulta el estado de tus reservas");
        List<Appointment> list = db.listAppointments();
        if (list.isEmpty()) {
            LinearLayout empty = softCard(); b.addView(empty); marginTop(empty, 18);
            empty.addView(title("Aún no tienes citas", 18));
            TextView t = text("Ve a Reservar para crear tu primera solicitud.", 14, MUTED); empty.addView(t); marginTop(t, 6);
            return;
        }
        for (Appointment a : list) {
            LinearLayout card = card(); b.addView(card); marginTop(card, 14);
            LinearLayout top = new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL);
            TextView dr = title(a.doctor, 18); top.addView(dr, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            top.addView(statusBadge(a.status)); card.addView(top);
            TextView spec = text(a.specialty, 14, MUTED); card.addView(spec); marginTop(spec, 6);
            TextView dt = text(a.date + " · " + a.time, 15, TEXT); dt.setTypeface(Typeface.DEFAULT_BOLD); card.addView(dt); marginTop(dt, 6);
            TextView rs = text("Motivo: " + a.reason, 14, MUTED); card.addView(rs); marginTop(rs, 6);
            if (!"Cancelada".equals(a.status)) {
                Button cancel = outlineButton("Cancelar cita"); card.addView(cancel); marginTop(cancel, 14);
                cancel.setOnClickListener(v -> { db.cancelAppointment(a.id); showAppointments(); });
            }
        }
    }

    private void showNotifications() {
        LinearLayout b = body(screen());
        addHeader(b, "Notificaciones", "Avisos del establecimiento de salud");
        List<String[]> items = db.listNotifications();
        for (String[] n : items) {
            LinearLayout card = card(); b.addView(card); marginTop(card, 14);
            card.addView(title(n[0], 17));
            TextView body = text(n[1], 14, TEXT); card.addView(body); marginTop(body, 6);
            TextView when = text(n[2], 12, MUTED); card.addView(when); marginTop(when, 8);
        }
    }

    private void showProfile() {
        selectTab(3);
        LinearLayout b = body(screen());
        addHeader(b, "Mi perfil", "Información del paciente");
        LinearLayout profile = card(); b.addView(profile); marginTop(profile, 18);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(logo(64));
        LinearLayout info = column();
        info.addView(title(session.getString("name", "Juan Pérez"), 20));
        TextView role = text("Paciente registrado", 14, MUTED); info.addView(role); marginTop(role, 4);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); infoLp.leftMargin = dp(12); row.addView(info, infoLp);
        profile.addView(row);
        LinearLayout data = softCard(); profile.addView(data); marginTop(data, 16);
        dataLine(data, "DNI", session.getString("dni", "12345678"));
        dataLine(data, "Teléfono", session.getString("phone", "999 999 999"));
        dataLine(data, "Correo", session.getString("email", "paciente@citasalud.pe"));
        Button logout = outlineButton("Cerrar sesión"); profile.addView(logout); marginTop(logout, 18);
        logout.setOnClickListener(v -> { session.edit().putBoolean("logged", false).apply(); showLogin(); });
    }

    private void addHeader(LinearLayout b, String main, String sub) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(logo(48));
        LinearLayout texts = column(); texts.addView(title(main, 23)); TextView s = text(sub, 13, MUTED); texts.addView(s); marginTop(s, 2);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); lp.leftMargin = dp(12); row.addView(texts, lp);
        b.addView(row);
    }

    private LinearLayout statCard(String label, String value, String sub) {
        LinearLayout c = softCard();
        c.addView(text(label, 13, MUTED)); TextView v = title(value, 24); c.addView(v); marginTop(v, 5); TextView s = text(sub, 12, MUTED); c.addView(s); marginTop(s, 4); return c;
    }

    private TextView statusBadge(String status) {
        if ("Cancelada".equals(status)) return badge(status, Color.rgb(254, 228, 226), DANGER);
        if ("Confirmada".equals(status)) return badge(status, Color.rgb(223, 247, 234), SUCCESS);
        return badge(status, Color.rgb(255, 241, 204), WARNING);
    }

    private TextView badge(String label, int fill, int color) {
        TextView v = text(label, 12, color); v.setTypeface(Typeface.DEFAULT_BOLD); v.setPadding(dp(11), dp(6), dp(11), dp(6)); v.setBackground(bg(fill, 50)); return v;
    }

    private void dataLine(LinearLayout p, String label, String value) {
        TextView l = text(label, 12, MUTED); p.addView(l); if (p.getChildCount() > 1) marginTop(l, 12);
        TextView v = text(value, 15, TEXT); v.setTypeface(Typeface.DEFAULT_BOLD); p.addView(v); marginTop(v, 3);
    }

    private Spinner spinner(String[] values) {
        Spinner s = new Spinner(this); s.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, values)); return s;
    }

    private TextView label(String s) { TextView t = text(s, 13, MUTED); t.setTypeface(Typeface.DEFAULT_BOLD); return t; }

    private ScrollView screen() {
        content.removeAllViews();
        ScrollView sv = new ScrollView(this); sv.setFillViewport(true);
        LinearLayout body = column(); body.setPadding(dp(18), dp(18), dp(18), dp(20));
        sv.addView(body, matchWrap()); content.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); sv.setTag(body); return sv;
    }
    private LinearLayout body(ScrollView sv) { return (LinearLayout) sv.getTag(); }
    private TextView sectionTitle(String s) { TextView t = title(s, 18); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.topMargin = dp(22); lp.bottomMargin = dp(10); t.setLayoutParams(lp); return t; }
    private LinearLayout column() { LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); return l; }

    private ImageView logo(int sizeDp) {
        ImageView i = new ImageView(this); i.setImageResource(R.drawable.ic_launcher); i.setScaleType(ImageView.ScaleType.FIT_CENTER); i.setLayoutParams(new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp))); return i;
    }

    private LinearLayout card() { LinearLayout c = column(); c.setPadding(dp(18), dp(18), dp(18), dp(18)); c.setBackground(outline(SURFACE, BORDER, 22)); if (Build.VERSION.SDK_INT >= 21) c.setElevation(dp(3)); return c; }
    private LinearLayout softCard() { LinearLayout c = column(); c.setPadding(dp(16), dp(16), dp(16), dp(16)); c.setBackground(bg(SOFT, 20)); return c; }

    private TextView title(String s, int sp) { TextView v = new TextView(this); v.setText(s); v.setTextColor(TEXT); v.setTextSize(sp); v.setTypeface(Typeface.DEFAULT_BOLD); return v; }
    private TextView text(String s, int sp, int color) { TextView v = new TextView(this); v.setText(s); v.setTextColor(color); v.setTextSize(sp); return v; }

    private EditText input(String hint) {
        EditText e = new EditText(this); e.setHint(hint); e.setSingleLine(true); e.setTextSize(16); e.setTextColor(TEXT); e.setHintTextColor(MUTED); e.setPadding(dp(16), 0, dp(16), 0); e.setBackground(outline(SURFACE, BORDER, 18)); e.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(56))); return e;
    }
    private Button primaryButton(String label) { Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT_BOLD); b.setBackground(gradient(PRIMARY, SECONDARY, 18)); b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))); if (Build.VERSION.SDK_INT >= 21) b.setElevation(dp(2)); return b; }
    private Button outlineButton(String label) { Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextColor(PRIMARY_DARK); b.setTextSize(15); b.setTypeface(Typeface.DEFAULT_BOLD); b.setBackground(outline(SURFACE, BORDER, 18)); b.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(54))); return b; }
    private Button chipButton(String label) { Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(13); b.setTextColor(PRIMARY_DARK); b.setBackground(outline(SURFACE, BORDER, 16)); return b; }

    private GradientDrawable bg(int color, int radius) { GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(radius)); return g; }
    private GradientDrawable outline(int fill, int stroke, int radius) { GradientDrawable g = bg(fill, radius); g.setStroke(dp(1), stroke); return g; }
    private GradientDrawable gradient(int start, int end, int radius) { GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{start, end}); g.setCornerRadius(dp(radius)); return g; }
    private void marginTop(View v, int value) { ViewGroup.LayoutParams p = v.getLayoutParams(); if (p instanceof ViewGroup.MarginLayoutParams) { ((ViewGroup.MarginLayoutParams) p).topMargin = dp(value); v.setLayoutParams(p); } }
    private ViewGroup.LayoutParams matchWrap() { return new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private String firstName(String full) { String[] p = full.trim().split("\\s+"); return p.length > 0 ? p[0] : full; }

    private static class Appointment {
        long id; String specialty, doctor, date, time, reason, status;
        Appointment(long id, String specialty, String doctor, String date, String time, String reason, String status) { this.id=id; this.specialty=specialty; this.doctor=doctor; this.date=date; this.time=time; this.reason=reason; this.status=status; }
    }

    private static class Db extends SQLiteOpenHelper {
        Db(Context c) { super(c, "citasalud.db", null, 1); }
        @Override public void onCreate(SQLiteDatabase d) {
            d.execSQL("CREATE TABLE appointments(id INTEGER PRIMARY KEY AUTOINCREMENT,specialty TEXT NOT NULL,doctor TEXT NOT NULL,date TEXT NOT NULL,time TEXT NOT NULL,reason TEXT NOT NULL,status TEXT NOT NULL)");
            d.execSQL("CREATE TABLE notifications(id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,body TEXT NOT NULL,created_at TEXT NOT NULL)");
            ContentValues n = new ContentValues(); n.put("title","Bienvenido a CitaSalud"); n.put("body","Desde aquí podrás reservar y consultar tus citas médicas."); n.put("created_at","Hoy"); d.insert("notifications", null, n);
        }
        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
        long addAppointment(String specialty, String doctor, String date, String time, String reason) {
            ContentValues v = new ContentValues(); v.put("specialty",specialty); v.put("doctor",doctor); v.put("date",date); v.put("time",time); v.put("reason",reason); v.put("status","Pendiente"); long id=getWritableDatabase().insert("appointments",null,v); addNotification("Solicitud enviada","Tu cita para "+date+" a las "+time+" quedó pendiente de confirmación."); return id;
        }
        List<Appointment> listAppointments() {
            ArrayList<Appointment> out=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT id,specialty,doctor,date,time,reason,status FROM appointments ORDER BY id DESC",null); try { while(c.moveToNext()) out.add(new Appointment(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getString(4),c.getString(5),c.getString(6))); } finally { c.close(); } return out;
        }
        void cancelAppointment(long id) { ContentValues v=new ContentValues(); v.put("status","Cancelada"); getWritableDatabase().update("appointments",v,"id=?",new String[]{String.valueOf(id)}); addNotification("Cita cancelada","La cita seleccionada fue cancelada desde tu aplicación."); }
        void addNotification(String title,String body) { ContentValues n=new ContentValues(); n.put("title",title); n.put("body",body); n.put("created_at","Ahora"); getWritableDatabase().insert("notifications",null,n); }
        List<String[]> listNotifications() { ArrayList<String[]> out=new ArrayList<>(); Cursor c=getReadableDatabase().rawQuery("SELECT title,body,created_at FROM notifications ORDER BY id DESC",null); try { while(c.moveToNext()) out.add(new String[]{c.getString(0),c.getString(1),c.getString(2)}); } finally { c.close(); } return out; }
    }
}
