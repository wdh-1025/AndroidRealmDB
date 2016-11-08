package com.realmdb;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.realmdb.bean.Book;
import com.realmdb.bean.BookDirectory;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Realm realm = Realm.getDefaultInstance();

    private Button btn_add, btn_add_transaction;
    private Button btn_delete;
    private Button btn_query_all, btn_query_where, btn_query_sort;
    private Button btn_update;
    private Button btn_add_async;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        btn_add_transaction = (Button) findViewById(R.id.btn_add_transaction);
        btn_add_transaction.setOnClickListener(this);

        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_delete.setOnClickListener(this);

        btn_query_all = (Button) findViewById(R.id.btn_query_all);
        btn_query_all.setOnClickListener(this);
        btn_query_where = (Button) findViewById(R.id.btn_query_where);
        btn_query_where.setOnClickListener(this);
        btn_query_sort = (Button) findViewById(R.id.btn_query_sort);
        btn_query_sort.setOnClickListener(this);

        btn_update = (Button) findViewById(R.id.btn_update);
        btn_update.setOnClickListener(this);

        btn_add_async = (Button) findViewById(R.id.btn_add_async);
        btn_add_async.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                add();
                break;
            case R.id.btn_add_transaction:
                add_transaction();
                break;

            case R.id.btn_delete:
                delete();
                break;

            case R.id.btn_query_all:
                queryAll();
                break;
            case R.id.btn_query_where:
                queryWhere();
                break;
            case R.id.btn_query_sort:
                querySort();
                break;

            case R.id.btn_update:
                update();
                break;

            case R.id.btn_add_async:
                addAsync();
                break;
        }
    }

    /**
     * 新建对象并进行存储
     */
    private void add() {
        realm.beginTransaction();
        //如果实体类使用字段@PrimaryKey注解的话需要注意的是
        //realm.createObject(Book.class，keyValue);需要同时传入keyValue且是唯一的，如果值已经存在，
        //此时会发生io.realm.exceptions.RealmPrimaryKeyConstraintException: Value already exists: 1错误，需要处理
        Book book = realm.createObject(Book.class);
        book.setBookName("我的第一本书");
        book.setBookPrice(20);
        //这里需要注意，对于多对多或者一对多的关系的话必须要先创建realm.createObject(XXX.class);
        BookDirectory bookDirectory = realm.createObject(BookDirectory.class);
        bookDirectory.setName("简介");
        bookDirectory.setDescribe("这本是的书名叫《我的第一本书》");
        RealmList<BookDirectory> directories = new RealmList<>();
        directories.add(bookDirectory);
        book.setDirectorys(directories);
        realm.commitTransaction();
    }


    /**
     * 使用事务块
     */
    private void add_transaction() {
        final Book book = new Book();
        book.setBookName("我的第二本书");
        book.setBookPrice(40);
        BookDirectory bookDirectory = new BookDirectory();
        bookDirectory.setName("简介");
        bookDirectory.setDescribe("这本是的书名叫《我的第二本书》");
        RealmList<BookDirectory> directories = new RealmList<>();
        directories.add(bookDirectory);
        book.setDirectorys(directories);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(book);
            }
        });
    }

    /**
     * 删除所有数据
     */
    private void delete() {
        final RealmResults<Book> books = realm.where(Book.class).findAll();
        realm.executeTransaction(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                //删除所有数据
                books.deleteAllFromRealm();
                /*//删除指定item数据
                Book book = books.get(0);
                book.deleteFromRealm();
                //删除第一个数据
                books.deleteFirstFromRealm();
                //删除最后一个数据
                books.deleteLastFromRealm();
                //删除position为0的数据
                books.deleteFromRealm(0);*/
                Toast.makeText(MainActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 查询全部数据
     */
    private void queryAll() {
        RealmResults<Book> books = realm.where(Book.class).findAll();
        List<Book> bookList = realm.copyFromRealm(books);
        Toast.makeText(this, "共查询到" + bookList.size() + "条记录", Toast.LENGTH_SHORT).show();
    }

    /**
     * 条件查询,多条件查询则在后面追加equalTo即可
     */
    private void queryWhere() {
        Book book = realm.where(Book.class).equalTo("bookName", "我的第一本书").findFirst();
        if (book != null) {
            book = realm.copyFromRealm(book);//转换为可直接直接调用的对象
            Toast.makeText(this, "查询书名为“我的第一本书”找到了", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "查询书名为“我的第一本书”找不到，试试先添加纪录再找", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 对查询结果进行排序
     */
    private void querySort() {
        RealmResults<Book> books = realm.where(Book.class).findAll();
        //增序排序
        books = books.sort("bookPrice");
        //降序排序
        //books = books.sort("bookPrice", Sort.DESCENDING);
        List<Book> bookList = realm.copyFromRealm(books);
        Toast.makeText(this, "查询到了" + bookList.size() + "条数据，已排序", Toast.LENGTH_SHORT).show();
        /**
         * 查询所有书的平均价格
         * realm.where(Book.class).findAll().average("bookPrice");
         * 查询所有书的总价格
         * realm.where(Book.class).findAll().sum("bookPrice");
         * 查询价格最高的书
         * realm.where(Book.class).findAll().max("bookPrice");
         * 查询价格最低的书
         * realm.where(Book.class).findAll().min("bookPrice");
         * 其它查询自行扩展
         */
    }

    /**
     * 更新数据
     */
    private void update() {
        Book book = realm.where(Book.class).equalTo("bookName", "我的第一本书").findFirst();
        if (book != null) {
            realm.beginTransaction();
            book.setBookName("我的第N本书");
            realm.commitTransaction();
            Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "查询书名为“我的第一本书”找不到，试试先添加纪录再找", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 异步增加
     */
    private RealmAsyncTask addTask;

    private void addAsync() {
        //对应的增删查改类似
        final Book book = new Book();
        book.setBookName("我的第三本书");
        book.setBookPrice(60);
        BookDirectory bookDirectory = new BookDirectory();
        bookDirectory.setName("简介");
        bookDirectory.setDescribe("这本是的书名叫《我的第三本书》");
        RealmList<BookDirectory> directories = new RealmList<>();
        directories.add(bookDirectory);
        book.setDirectorys(directories);
        addTask = realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(book);
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
            }
        }, new Realm.Transaction.OnError() {

            @Override
            public void onError(Throwable error) {
                Toast.makeText(MainActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (addTask != null && addTask.isCancelled()) {
            addTask.cancel();//取消掉异步任务
        }
        if (realm != null) {
            realm.close();
        }
    }
}
