# ImageStory - Android Custom View ala Story

`ImageStory` adalah komponen tampilan khusus (custom view) untuk Android yang memungkinkan developer menampilkan gambar dengan border melingkar yang terinspirasi dari fitur "story" di aplikasi media sosial seperti Instagram dan WhatsApp. Library ini juga dilengkapi dengan kemampuan untuk memeriksa apakah file gambar sudah ada secara lokal, dan jika belum, akan mengunduh otomatis dari URL serta menyimpannya ke direktori `Download`.

## Fitur Utama

- Menampilkan gambar dengan border ala story (lingkaran)
- Jumlah border dapat disesuaikan
- Status story sudah/belum dibaca (warna berbeda)
- Cek dan load file lokal sebelum download dari internet
- Otomatis simpan gambar hasil download ke storage
- Dukungan Android 10 (Q) dan di atasnya tanpa izin penyimpanan tambahan
- Kustomisasi penuh melalui XML dan kode Java

## Instalasi

Tambahkan repository `jitpack.io` pada `settings.gradle` atau `build.gradle` project-level:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Kemudian tambahkan dependensi di `build.gradle` modul kamu:

```gradle
implementation("com.github.chaerul24:ImageStory:v1.0.0")
```

> Pastikan versi disesuaikan dengan rilis di GitHub kamu.

## Penggunaan

### XML Layout

```xml
<id.chaerul.library.imagestory.ImageStory
        android:id="@+id/imageStory"
<<<<<<< HEAD
        android:layout_width="80dp"
        android:layout_height="80dp"
=======
        android:layout_width="70dp"
        android:layout_height="70dp"
>>>>>>> 1d85355 (Add scale attribute and handle image scale type)
        app:src="@drawable/ic_launcher_background"
        app:radius="50dp"
        app:borderWidth="3dp"
        app:countBorder="3"
        app:borderColor="#4CAF50"
        app:borderColorHint="#BDBDBD" />
```

### Inisialisasi dari Java

```java
ImageStory imageStory = findViewById(R.id.imageStory);
imageStory.setCountBorder(3);

boolean[] statusRead = new boolean[]{false, true, false};
imageStory.setRead(statusRead);
```

### Cek dan Download File Gambar dari URL

```java
String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        
        imageStory = findViewById(R.id.imageStory);
        imageStory.setCheckFile(
                "https://avatars.githubusercontent.com/u/160778594?v=4",
                path,
                new ImageStory.DownloadCallback() {
                    @Override
                    public void onDownloaded(String s) {
                        Log.d("path", s);
                    }

                    @Override
                    public void onError(String s) {
                        Log.e("path", s);
                    }
                }
        );

        imageStory.setRead(new boolean[] {false, true, false});
```

## Lisensi

MIT License

---

**Dibuat dengan ❤️ oleh [Chaerul](https://github.com/chaeruldev)**

Silakan kontribusi, fork, atau kasih bintang ⭐ kalau kamu suka project ini!

