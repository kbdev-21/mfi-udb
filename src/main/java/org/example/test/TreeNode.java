package org.example.test;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    int val;
    List<TreeNode> children;

    public TreeNode(int val) {
        this.val = val;
        this.children = new ArrayList<>();
    }
}
