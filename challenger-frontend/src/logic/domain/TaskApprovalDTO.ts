
export interface TaskApprovalDTO {
    userId: number,
    taskId: number,
    taskStatus: string,
    rejectionReason?: string

}