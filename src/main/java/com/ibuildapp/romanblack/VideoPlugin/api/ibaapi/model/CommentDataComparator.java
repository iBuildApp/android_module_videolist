package com.ibuildapp.romanblack.VideoPlugin.api.ibaapi.model;


import java.util.Comparator;

public class CommentDataComparator implements Comparator<CommentData> {
    @Override
    public int compare(CommentData lhs, CommentData rhs) {
        Long lhsLong = Long.valueOf(lhs.getCreate());
        Long rhsLong = Long.valueOf(rhs.getCreate());

        return lhsLong < rhsLong ? 1 : (lhsLong == rhsLong ? 0 : -1);
    }
}
