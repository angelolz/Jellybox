package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.examples.doc.Author;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.junit.JUnitTestRule;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tools.MockTextChannelCreate.createMockTextChannel;

public class TestPing {

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void pingSuccessTest(){
        ArgumentCaptor<String> textChannelArgumentCaptor = ArgumentCaptor.forClass(String.class);
        TextChannel mockTestChannel = createMockTextChannel(textChannelArgumentCaptor);

        User mockUser = mock(User.class);
        when(mockUser.getAsMention()).thenReturn("<@189690228292845568>");

        CommandEvent mockCommandEvent = mock(CommandEvent.class);

        when(mockCommandEvent.getChannel()).thenReturn(mockTestChannel);
        when(mockCommandEvent.getAuthor()).thenReturn(mockUser);

        Ping testPing = new Ping();
        testPing.execute(mockCommandEvent);

        assertNotNull(textChannelArgumentCaptor.getValue());
    }
}
