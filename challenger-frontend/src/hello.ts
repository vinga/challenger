
interface Todo {
    id?: number;
    text: string;
    completed: boolean;
}

// Valid assignment.
export const t1: Todo = {
    id: 1,
    text: 'Finish this blog post',
    completed: false
};

/*
 // Type error because text is missing.
 const t2: Todo = {
 id: 2,
 completed: false
 };*/
