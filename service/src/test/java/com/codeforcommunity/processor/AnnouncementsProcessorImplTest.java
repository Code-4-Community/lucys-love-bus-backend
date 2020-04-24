package com.codeforcommunity.processor;

import com.codeforcommunity.JooqMock;
import org.junit.Before;

// Contains tests for AnnouncementsProcessorImpl.java in main
public class AnnouncementsProcessorImplTest {
    JooqMock myJooqMock;
    AnnouncementsProcessorImpl myAnnouncementsProcessorImpl;

    // set up all the mocks
    @Before
    public void setup() {
        this.myJooqMock = new JooqMock();
        this.myAnnouncementsProcessorImpl = new AnnouncementsProcessorImpl(myJooqMock.getContext());
    }
}