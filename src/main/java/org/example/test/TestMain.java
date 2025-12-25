package org.example.test;

import java.util.ArrayList;
import java.util.List;

public class TestMain {
    private static List<Integer> nums = new ArrayList<>();

    public static void main(String[] args) {
        TreeNode root = new TreeNode(1);

        TreeNode node2 = new TreeNode(2);
        TreeNode node3 = new TreeNode(3);
        TreeNode node4 = new TreeNode(4);

        TreeNode node5 = new TreeNode(5);
        TreeNode node6 = new TreeNode(6);

        root.children.add(node2);
        root.children.add(node3);
        root.children.add(node4);

        node3.children.add(node5);
        node3.children.add(node6);

        dfs(root);

        System.out.println(nums);
    }

    private static void dfs(TreeNode node) {
        if(node == null) return;

        nums.add(node.val);

        for(TreeNode child : node.children) {
            dfs(child);
        }
    }
}
