package cz.cuni.mff.xrg.odalic.api.rest.responses;

import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


/**
 * A {@link Message} builder.
 * 
 * @author Václav Brodec
 *
 */
public final class MessageBuilder {

  private String text;

  private List<URI> additionalResources;

  private String debugContent;

  public MessageBuilder() {
    text = null;
    additionalResources = ImmutableList.of();
    debugContent = null;
  }
    
  /**
   * @return the text
   */
  @Nullable
  public String getText() {
    return text;
  }

  /**
   * @param text the text to set
   */
  public MessageBuilder text(String text) {
    this.text = text;
    
    return this;
  }

  /**
   * @return the additional resources
   */
  @Nullable
  public List<URI> getAdditionalResources() {
    return additionalResources;
  }

  /**
   * @param additionalResources the additional resources to set
   */
  public MessageBuilder additionalResources(List<? extends URI> additionalResources) {
    Preconditions.checkNotNull(additionalResources);
    
    this.additionalResources = ImmutableList.copyOf(additionalResources);
    
    return this;
  }
  
  /**
   * @return the debug content
   */
  @Nullable
  public String getDebugContent() {
    return debugContent;
  }

  /**
   * @param debugContent the debug content to set
   */
  public MessageBuilder debugContent(String debugContent) {
    this.debugContent = debugContent;
    
    return this;
  }

  public Message build() {
    return new Message(text, additionalResources, debugContent);
  }
  
  public MessageBuilder reset() {
    text = null;
    additionalResources = null;
    debugContent = null;
    
    return this;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MessageBuilder [text=" + text + ", additionalResources=" + additionalResources
        + ", debugContent=" + debugContent + "]";
  }
}
