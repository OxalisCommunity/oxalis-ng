package org.apache.cxf.attachment;

import org.apache.cxf.message.Attachment;

import jakarta.activation.DataHandler;
import java.io.IOException;
import java.util.*;

public class As4LazyAttachmentCollection implements Collection<Attachment> {

    private As4AttachmentDeserializer deserializer;
    private final List<Attachment> attachments = new ArrayList<>();
    private final int maxAttachmentCount;

    public As4LazyAttachmentCollection(As4AttachmentDeserializer deserializer, int maxAttachmentCount) {
        super();
        this.deserializer = deserializer;
        this.maxAttachmentCount = maxAttachmentCount;
    }

    public List<Attachment> getLoadedAttachments() {
        return attachments;
    }

    private void loadAll() {
        try {
            Attachment a = deserializer.readNext();
            int count = 0;
            while (a != null) {
                attachments.add(a);
                count++;
                if (count > maxAttachmentCount) {
                    throw new IOException("The message contains more attachments than are permitted");
                }
                a = deserializer.readNext();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Check for more attachments by attempting to deserialize the next attachment.
     *
     * @param shouldLoadNew if <i>false</i>, the "loaded attachments" List will not be changed.
     * @return there is more attachment or not
     * @throws IOException
     */
    public boolean hasNext(boolean shouldLoadNew) throws IOException {
        if (shouldLoadNew) {
            Attachment a = deserializer.readNext();
            if (a != null) {
                attachments.add(a);
                return true;
            }
            return false;
        }
        return deserializer.hasNext();
    }

    public boolean hasNext() throws IOException {
        return hasNext(true);
    }

    public Iterator<Attachment> iterator() {
        return new Iterator<Attachment>() {
            int current;
            boolean removed;

            public boolean hasNext() {
                if (attachments.size() > current) {
                    return true;
                }

                // check if there is another attachment
                try {
                    Attachment a = deserializer.readNext();
                    if (a == null) {
                        return false;
                    }
                    attachments.add(a);
                    return true;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public Attachment next() {
                Attachment a = attachments.get(current);
                current++;
                removed = false;
                return a;
            }

            public void remove() {
                if (removed) {
                    throw new IllegalStateException();
                }
                deserializer.addRemoved(attachments.remove(--current));
                removed = true;
            }

        };
    }

    public int size() {
        loadAll();

        return attachments.size();
    }

    public boolean add(Attachment arg0) {
        return attachments.add(arg0);
    }

    public boolean addAll(Collection<? extends Attachment> arg0) {
        return attachments.addAll(arg0);
    }

    public void clear() {
        attachments.clear();
    }

    public boolean contains(Object arg0) {
        return attachments.contains(arg0);
    }

    public boolean containsAll(Collection<?> arg0) {
        return new HashSet<>(attachments).containsAll(arg0);
    }

    public boolean isEmpty() {
        if (attachments.isEmpty()) {
            return !iterator().hasNext();
        }
        return attachments.isEmpty();
    }

    public boolean remove(Object arg0) {
        return attachments.remove(arg0);
    }

    public boolean removeAll(Collection<?> arg0) {
        return attachments.removeAll(arg0);
    }

    public boolean retainAll(Collection<?> arg0) {
        return attachments.retainAll(arg0);
    }

    public Object[] toArray() {
        loadAll();

        return attachments.toArray();
    }

    public <T> T[] toArray(T[] arg0) {
        loadAll();

        return attachments.toArray(arg0);
    }

    public Map<String, DataHandler> createDataHandlerMap() {
        return new As4LazyAttachmentCollection.LazyAttachmentMap(this);
    }

    private static class LazyAttachmentMap implements Map<String, DataHandler> {
        As4LazyAttachmentCollection collection;

        LazyAttachmentMap(As4LazyAttachmentCollection c) {
            collection = c;
        }

        public void clear() {
            collection.clear();
        }

        public boolean containsKey(Object key) {
            Iterator<Attachment> it = collection.iterator();
            while (it.hasNext()) {
                Attachment at = it.next();
                if (key.equals(at.getId())) {
                    return true;
                }
            }
            return false;
        }

        public boolean containsValue(Object value) {
            Iterator<Attachment> it = collection.iterator();
            while (it.hasNext()) {
                Attachment at = it.next();
                if (value.equals(at.getDataHandler())) {
                    return true;
                }
            }
            return false;
        }

        public DataHandler get(Object key) {
            Iterator<Attachment> it = collection.iterator();
            while (it.hasNext()) {
                Attachment at = it.next();
                if (key.equals(at.getId())) {
                    return at.getDataHandler();
                }
            }
            return null;
        }

        public boolean isEmpty() {
            return collection.isEmpty();
        }

        public int size() {
            return collection.size();
        }

        public DataHandler remove(Object key) {
            Iterator<Attachment> it = collection.iterator();
            while (it.hasNext()) {
                Attachment at = it.next();
                if (key.equals(at.getId())) {
                    collection.remove(at);
                    return at.getDataHandler();
                }
            }
            return null;
        }

        public DataHandler put(String key, DataHandler value) {
            Attachment at = new AttachmentImpl(key, value);
            collection.add(at);
            return value;
        }

        public void putAll(Map<? extends String, ? extends DataHandler> t) {
            for (Map.Entry<? extends String, ? extends DataHandler> ent : t.entrySet()) {
                put(ent.getKey(), ent.getValue());
            }
        }


        public Set<Map.Entry<String, DataHandler>> entrySet() {
            return new AbstractSet<Map.Entry<String, DataHandler>>() {
                public Iterator<Map.Entry<String, DataHandler>> iterator() {
                    return new Iterator<Map.Entry<String, DataHandler>>() {
                        Iterator<Attachment> it = collection.iterator();

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public Map.Entry<String, DataHandler> next() {
                            return new Map.Entry<String, DataHandler>() {
                                Attachment at = it.next();

                                public String getKey() {
                                    return at.getId();
                                }

                                public DataHandler getValue() {
                                    return at.getDataHandler();
                                }

                                public DataHandler setValue(DataHandler value) {
                                    if (at instanceof AttachmentImpl) {
                                        DataHandler h = at.getDataHandler();
                                        ((AttachmentImpl) at).setDataHandler(value);
                                        return h;
                                    }
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }

                        public void remove() {
                            it.remove();
                        }
                    };
                }

                public int size() {
                    return collection.size();
                }
            };
        }

        public Set<String> keySet() {
            return new AbstractSet<String>() {
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        Iterator<Attachment> it = collection.iterator();

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public String next() {
                            return it.next().getId();
                        }

                        public void remove() {
                            it.remove();
                        }
                    };
                }

                public int size() {
                    return collection.size();
                }
            };
        }


        public Collection<DataHandler> values() {
            return new AbstractCollection<DataHandler>() {
                public Iterator<DataHandler> iterator() {
                    return new Iterator<DataHandler>() {
                        Iterator<Attachment> it = collection.iterator();

                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        public DataHandler next() {
                            return it.next().getDataHandler();
                        }

                        public void remove() {
                            it.remove();
                        }
                    };
                }

                public int size() {
                    return collection.size();
                }
            };
        }

    }
}
