package com.example.project;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Contact implements Serializable {
    private String number;
    private String name;
    private ContentResolver contentResolver;

    public Contact(String name, String number){
        this.name = name;
        this.number = number;
    }

    public Contact(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Contact contact = (Contact) obj;
        return Objects.equals(name, contact.name) && Objects.equals(normalizeNumber(number), normalizeNumber(contact.number));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, normalizeNumber(number));
    }

    private String normalizeNumber(String number) {
        String normalizedNumber = number.replaceAll("^8", "+7")
                .replaceAll("^7", "+7")
                .replaceAll("[\\s-]", "");
        return normalizedNumber;
    }

    public String getNumber(){
        return number;
    }
    public String getName(){
        return name;
    }

    public void getContactsList() {
        Set<Contact> setContacts = new HashSet<>();
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone._ID};

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            if (nameIndex >= 0 && numberIndex >= 0) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    String number = cursor.getString(numberIndex);

                    Contact contact = new Contact(name, normalizeNumber(number));
                    setContacts.add(contact);
                }

                if (MainActivity.contacts != null) {
                    MainActivity.contacts.clear();
                }
                MainActivity.contacts = new ArrayList<>(setContacts);
            }
            cursor.close();
        }
    }
}
