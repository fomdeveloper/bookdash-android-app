package org.bookdash.android.presentation.downloads;

import com.parse.ParseObject;

import org.bookdash.android.data.books.BookDetailRepository;
import org.bookdash.android.domain.pojo.BookDetail;
import org.bookdash.android.domain.pojo.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;


public class DownloadsPresenterTest {
    @Mock
    private BookDetailRepository bookRepository;

    private DownloadsPresenter downloadsPresenter;

    @Mock
    private DownloadsContract.View downloadsView;
    @Captor
    private ArgumentCaptor<BookDetailRepository.GetBooksForLanguageCallback> bookloadedCaptor;
    @Captor
    private ArgumentCaptor<BookDetailRepository.DeleteBookCallBack> deleteBookCallBackArgumentCaptor;
    private Language language;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ParseObject.registerSubclass(Language.class);
        ParseObject.registerSubclass(BookDetail.class);
        downloadsPresenter = new DownloadsPresenter(bookRepository, downloadsView);
        language = new Language("English", "EN", "1");
    }

    @After
    public void tearDown() throws Exception {

    }

    private List<BookDetail> BOOKS = new ArrayList<>();

    @Test
    public void testGetListDownloads_ReturnsDownloads() {

        downloadsPresenter.loadListDownloads();
        verify(downloadsView).showLoading(true);
        verify(bookRepository).getDownloadedBooks(bookloadedCaptor.capture());
        BOOKS.add(new BookDetail("test title", "test", "2", language));
        bookloadedCaptor.getValue().onBooksLoaded(BOOKS);

        verify(downloadsView).showLoading(false);
        verify(downloadsView).showDownloadedBooks(BOOKS);
    }

    @Test
    public void testGetListDownloads_Error_ReturnsErrorMessage() {
        downloadsPresenter.loadListDownloads();
        verify(downloadsView).showLoading(true);
        verify(bookRepository).getDownloadedBooks(bookloadedCaptor.capture());
        bookloadedCaptor.getValue().onBooksLoadError(new Exception("Blah books didn't load"));

        verify(downloadsView).showLoading(false);
        verify(downloadsView).showErrorScreen(true, "Blah books didn't load", true);
    }

    @Test
    public void testDeleteDownload_RemovesDownload() {
        BookDetail bookDetail = new BookDetail("Fake Book", "http://test.com", "123", language);
        downloadsPresenter.deleteDownload(bookDetail);

        verify(bookRepository).deleteBook(any(BookDetail.class), deleteBookCallBackArgumentCaptor.capture());
        deleteBookCallBackArgumentCaptor.getValue().onBookDeleted(bookDetail);

        verify(bookRepository).getDownloadedBooks(bookloadedCaptor.capture());
        bookloadedCaptor.getValue().onBooksLoaded(BOOKS);

        verify(downloadsView).showNoBooksDownloadedMessage();
    }

    @Test
    public void testDeleteDownload_RemovesDownloadKeepsOthers() {
        BookDetail bookDetail = new BookDetail("Fake Book", "http://test.com", "123", language);
        downloadsPresenter.deleteDownload(bookDetail);

        verify(bookRepository).deleteBook(any(BookDetail.class), deleteBookCallBackArgumentCaptor.capture());
        deleteBookCallBackArgumentCaptor.getValue().onBookDeleted(bookDetail);
        BOOKS.add(new BookDetail("test title", "book cover", "23", new Language()));
        verify(bookRepository).getDownloadedBooks(bookloadedCaptor.capture());
        bookloadedCaptor.getValue().onBooksLoaded(BOOKS);

        verify(downloadsView).showDownloadedBooks(BOOKS);
    }
}