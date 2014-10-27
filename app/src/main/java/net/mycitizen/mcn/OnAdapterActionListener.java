package net.mycitizen.mcn;

public interface OnAdapterActionListener {
    public void onTrashMessageAction(int message_id);

    public void onUntrashMessageAction(int message_id);

    public void onAcceptMessageAction(int message_id, int sender_id);

    public void onDeclineMessageAction(int message_id, int sender_id);

    public void onChangeTag(int tag_id, boolean status);
}
