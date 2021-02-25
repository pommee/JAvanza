import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.bastiaanjansen.otp.builders.TOTPGeneratorBuilder;
import com.github.eitraz.avanza.AvanzaApi;
import com.github.eitraz.avanza.model.account.Overview;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Main {

    final private static String userName = "";
    final private static String password = "";
    final private static String SECRET = "";
    final private static String WEBHOOK = "";
    private static double lastCheck = 0.0;

    public static void main(String[] args) throws InterruptedException, IOException {
        run();
    }

    private static void run() throws InterruptedException, IOException {
        if (checkDay()) {
            try {
                lastCheck = readFromFile();
                String code = getCode();
                AvanzaApi api = new AvanzaApi(userName, password, () -> code);
                // Get overview
                Overview overview = api.getOverview();
                Double amount = overview.getTotalOwnCapital();
                prepareMessage(amount);
                writeToFile(amount);
                TimeUnit.MINUTES.sleep(5); //Timer for how often it runs
                run();
            } catch (RuntimeException | UnsupportedEncodingException | URISyntaxException ignored) {
                TimeUnit.SECONDS.sleep(10); //Exception = retry in x seconds
                run();
            }
        } else
            TimeUnit.MINUTES.sleep(5);
    }

    private static String getCode() throws UnsupportedEncodingException, URISyntaxException {
        URI uri = new URI("otpauth://totp/issuer?secret=" + SECRET + "&algorithm=SHA1&digits=6&period=30");
        TOTPGenerator totp = TOTPGeneratorBuilder.fromOTPAuthURI(uri);
        System.out.println("Current code: " + totp.generate());
        return totp.generate();
    }

    private static void prepareMessage(Double amount) {
        if (lastCheck == 0.0) {
            int color = 0x808080; //Gray, used for first start
            String prefix = "+";
            String percent = "0.00";
            sendMessage(amount, color, percent, prefix);
        } else if (lastCheck > amount) {
            int color = 0xFF0000; //Red, if past value > current
            double decrease = lastCheck - amount;
            String percent = String.valueOf(decrease / amount * 100);
            String prefix = "-";
            sendMessage(amount, color, percent, prefix);
        } else if (lastCheck < amount) {
            int color = 0x7cfc00; //Green, if past value < current
            double increase = amount - lastCheck;
            String percent = String.valueOf(increase / amount * 100);
            String prefix = "+";
            sendMessage(amount, color, percent, prefix);
        }
    }

    private static void sendMessage(double amount, int color, String percent, String prefix) {
        WebhookClient client = WebhookClient.withUrl(WEBHOOK); // or withId(id, token)
        // Send and log (using embed)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String finalAmount;
        DecimalFormat betterFormat = new DecimalFormat("#,###.##");
        finalAmount = betterFormat.format(amount);
        String message = "```diff\n" + prefix + percent + "%" + "```";
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(color)
                .setDescription(formatter.format(date) + "\n\n" + "**" + "Total balance: " + finalAmount + "**" +
                        "\n\n" + "% Change since last check: " + message)
                .build();
        client.send(embed);
        lastCheck = amount;
    }

    @SuppressWarnings("deprecation")
    private static boolean checkDay() { //Check time and day
        Calendar day = Calendar.getInstance();
        if (!(day.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
                !(day.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
            int currentTime = new Date().getHours();
            int startTime = 8;
            int endTime = 22;

            return currentTime > startTime & currentTime < endTime;
        }
        return false;
    }

    private static void writeToFile(double lastCheck) throws IOException {
        File fout = new File("rem.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        bw.write(String.valueOf(lastCheck));
        bw.close();
    }

    private static double readFromFile() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "rem.txt"));
            String line = reader.readLine();
            if (line != null) {
                return Double.parseDouble(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
