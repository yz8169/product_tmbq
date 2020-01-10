#!/usr/bin/python
# -*- coding: UTF-8 -*-
from tkinter.ttk import *
from tkinter import filedialog
from tkinter import *
from scipy.signal import savgol_filter
import pandas as pd
import numpy as np
import threading
import matplotlib.pyplot as plt

size = "550x450"


def average_intensity(time_list, intensity_list, time_left, time_right):
    assert len(time_list) == len(intensity_list)
    if time_left != None and time_right != None:
        a = list(zip(time_list, intensity_list))
        b = [i for i in a if time_left <= i[0] <= time_right]
        int_lst = np.array([x[1] for x in b])
        return np.average(int_lst)


def smooth(intensity_list):
    j = 1
    while j <= 10:
        intensity_list = savgol_filter(intensity_list, 19, 4)
        j += 1
    return list(intensity_list)


class GetIntensity(object):
    def __init__(self, ms_file=None):
        self.ms_file = ms_file
        f = open(ms_file, "r")
        self.content = f.readlines()
        num_list = [idx for idx, i in enumerate(self.content) if i[0] == 'F']
        func_list, rang = [i for i in range(1, len(num_list) + 1)], {}
        for idx, i in enumerate(func_list[:-1]):
            rang[i] = (num_list[idx], num_list[idx + 1])
        rang[func_list[-1]] = (num_list[-1], len(self.content) + 1)
        self.range = rang

    @classmethod
    def split_intensity(cls, lst, total_n, value_index, t_lst):
        length = len(lst) / len(t_lst)
        if int(length) != total_n:
            return None
        intens_lst = []
        i = value_index
        while i <= len(lst) - 1:
            intens_lst.append(lst[i])
            i = i + total_n
        return intens_lst

    def get_profile(self, func, mass1):
        try:
            function, ms_value = func, mass1
            ion_lst = self.content[self.range[function][0]:self.range[function][1]]
            __intensity_list, __time_list = [], []
            for i in ion_lst:
                i = i.strip()
                if len(i) > 1:
                    if i[0] == "R":
                        __time_list.append(float(i.split('\t')[1]))
                    if "." in (i.split('\t')[0]):
                        if float(i.split('\t')[0]) == float(ms_value):
                            __intensity_list.append(float(i.split('\t')[1]))
            return __intensity_list, __time_list
        except KeyError:
            # 如果质谱数据中，例如wash, blank样本中， 没有对应的func和mass1
            return None

    def get_rts(self, __intensity_list, __time_list):
        local_maxs = []
        if __intensity_list:
            x = int(len(__intensity_list) * 0.2)  # 噪音强度是后百分之20的均值
            noise = np.mean(sorted(__intensity_list)[:x])
            __smoothed_intensity_list = smooth(__intensity_list)
            for i in range(1, len(__smoothed_intensity_list) - 1):
                if __smoothed_intensity_list[i] > __smoothed_intensity_list[i + 1] and __smoothed_intensity_list[i] >= \
                        __smoothed_intensity_list[i - 1] or \
                        __smoothed_intensity_list[i] >= __smoothed_intensity_list[i + 1] and __smoothed_intensity_list[
                    i] > __smoothed_intensity_list[i - 1]:
                    if __intensity_list[i] >= 3 * noise:  # 三倍信噪比
                        local_maxs.append((__time_list[i], __smoothed_intensity_list[i]))
            local_maxs = sorted(local_maxs, key=lambda _x: _x[1], reverse=True)
            return local_maxs
        return []


class GetAllRt(object):
    def __init__(self, compound_list=None, ms_file=None):
        self.com_profile_dict = {}
        self.ms_file = ms_file
        if ".xlsx" in compound_list or '.XLSX' in compound_list:
            data = pd.DataFrame(pd.read_excel(compound_list, usecols=[u'Compound', u'Function', u'Mass', u'RT']))
        if '.csv' in compound_list or '.CSV' in compound_list:
            data = pd.DataFrame(pd.read_csv(compound_list, usecols=[u'Compound', u'Function', u'Mass', u'RT']))
        com, fun, rt = [i for i in data['Compound']], [int(i) for i in data['Function']], [float(i) for i in data['RT']]
        self.com_rt_dict = {i[0]: i[1] for i in zip([i for i in data['Compound']], [i for i in data['RT']])}
        mass1, mass2 = [float(i.split(">")[0].strip()) for i in data['Mass']], \
                       [float(i.split(">")[1].strip()) for i in data['Mass']]
        f_m, f_m_m = list(zip(fun, mass1)), list(zip(fun, mass1, mass2))
        a = GetIntensity(ms_file=self.ms_file)
        self.f_m_profile_dict, self.single_nearest_rt = {}, {}
        for i in f_m:
            f, m = i
            if i not in self.f_m_profile_dict:
                self.f_m_profile_dict[i] = a.get_profile(f, m)

        self.com_idx = {}
        self.multi, self.single = [], []

        for idx, i in enumerate(f_m):
            f_m1_m2 = f_m_m[idx]
            if f_m_m.count(f_m1_m2) == 1:
                self.single.append(com[idx])
            else:
                self.multi.append(com[idx])
            _a = []
            f, m1, m2 = f_m1_m2
            _a.append(m2)
            for j in f_m_m:
                _f, _m1, _m2 = j
                if f == _f and m1 == _m1 and _m2 not in _a:
                    _a.append(_m2)
            _a = sorted(_a)
            idx1 = _a.index(m2)
            self.com_idx[com[idx]] = (f, m1, len(_a), idx1)

        self.multi_fmm_dict = {}

        for idx, i in enumerate(f_m):
            f_m1_m2 = f_m_m[idx]
            if f_m_m.count(f_m1_m2) > 1:
                compound = com[idx]
                self.multi_fmm_dict[compound] = f_m1_m2

        self.multi_rt_range_dict = {}
        for x in self.multi_fmm_dict:
            ion_parm = self.multi_fmm_dict[x]
            rt_lst = []
            for idx, i in enumerate(f_m_m):
                if ion_parm == i:
                    ren_time = rt[idx]
                    rt_lst.append(ren_time)
            if len(rt_lst) >= 2:
                self.multi_rt_range_dict[x] = (min(rt_lst), max(rt_lst))

        self.com_position = {}
        for i in self.multi:
            idx = com.index(i)
            rt = self.com_rt_dict[i]
            rt_lst = [rt]
            f_m1_m2 = f_m_m[idx]
            for x, y in enumerate(f_m_m):
                if f_m1_m2 == y:
                    rt1 = self.com_rt_dict[com[x]]
                    if rt1 not in rt_lst:
                        rt_lst.append(rt1)
            position = sorted(rt_lst).index(rt)
            func, m1, m2, n = f_m1_m2[0], f_m1_m2[1], f_m1_m2[2], len(rt_lst)
            self.com_position[i] = func, m1, m2, n, position

    def calculate_single_rt(self):
        b = GetIntensity(ms_file=self.ms_file)
        single_rt_dict = {}
        for i in self.single:
            func, ms, n_value, idx = self.com_idx[i]
            intensity_list, time_list = self.f_m_profile_dict[(func, ms)]
            intensity_list = b.split_intensity(intensity_list, n_value, idx, time_list)
            if intensity_list:
                self.com_profile_dict[i] = intensity_list, time_list
                local_maxs = b.get_rts(intensity_list, time_list)
                rt = self.com_rt_dict[i]
                local_maxs = [x for x in local_maxs if rt - 0.3 <= x[0] <= rt + 0.3]
                neighbor_local_maxs = [x for x in local_maxs if rt - 0.15 <= x[0] <= rt + 0.15]
                if len(local_maxs) > 0:
                    rt = self.com_rt_dict[i]
                    average_height = average_intensity(time_list, intensity_list, rt - 0.15, rt + 0.15)
                    if average_height > 10:
                        single_rt_dict[i] = local_maxs[0][0]
                        if len(neighbor_local_maxs) > 0:
                            neighbor_largest_rt = neighbor_local_maxs[0][0]
                            self.single_nearest_rt[i] = neighbor_largest_rt
        return single_rt_dict  # {物质：time}

    def calculate_multi_rt(self):
        b = GetIntensity(ms_file=self.ms_file)
        multi_rt_dict = {}
        two_rt_mid = {}
        for i in self.multi:
            func, m1, m2, n, position = self.com_position[i]
            for com in self.multi:
                _func, _m1, _m2, _n, _position = self.com_position[com]
                # 如果只有两个物质，需要确定两个物质的rt差异
                if com != i and func == _func and m1 == _m1 and m2 == _m2 and _n == n == 2:
                    rt_diff = self.com_rt_dict[i] - self.com_rt_dict[com]
                    if abs(rt_diff) >= 0.5:
                        rt_mid = (self.com_rt_dict[i] + self.com_rt_dict[com]) * 0.5
                        if self.com_rt_dict[i] > rt_mid:
                            two_rt_mid[i] = (rt_mid, 'right')
                        if self.com_rt_dict[i] <= rt_mid:
                            two_rt_mid[i] = (rt_mid, 'left')

        for i in self.multi:
            f, m1, total_n, idx = self.com_idx[i]
            __intensity_list, __time_list = self.f_m_profile_dict[(f, m1)]
            __intensity_list = b.split_intensity(__intensity_list, total_n, idx, __time_list)
            if __intensity_list:
                self.com_profile_dict[i] = __intensity_list, __time_list
            all_rt = b.get_rts(__intensity_list, __time_list)

            if all_rt:
                # 如果某个物质是双物质的其中之一，且二者的差异大于0.5，则分别在左右两个区域进行局部最大点的寻找
                if i in two_rt_mid:
                    rt_mid_value, rt_area = two_rt_mid[i]
                    # 如果该物质在右半边，则在右半边区域寻找最大的极值点，截断点是rt均值
                    if rt_area == 'right':
                        rt_right_lst = [x[0] for x in all_rt if x[0] > rt_mid_value]
                        if len(rt_right_lst) > 0:
                            multi_rt_dict[i] = rt_right_lst[0]

                    # 如果该物质在左半边，则在左半边区域寻找最大的极值点
                    if rt_area == 'left':
                        rt_left_lst = [x[0] for x in all_rt if x[0] <= rt_mid_value]
                        if len(rt_left_lst) > 0:
                            multi_rt_dict[i] = rt_left_lst[0]

                else:
                    num, position = self.com_position[i][3], self.com_position[i][4]
                    # 如果峰的总数超过了总物质数量，提取出高度topN的峰
                    if len(all_rt) >= num:
                        # pick top num peak
                        all_rt = [x for x in all_rt if
                                  self.multi_rt_range_dict[i][0] - 0.3 <= x[0] <= self.multi_rt_range_dict[i][1] + 0.3]
                        rt_lst = all_rt[:num]
                        rt_lst = [_[0] for _ in rt_lst]
                        rt_lst = sorted(rt_lst)
                        multi_rt_dict[i] = rt_lst[position]
            else:
                pass
        return multi_rt_dict


class ModifyCsv(object):
    def __init__(self, compound_list=None, ms_file1=None, ms_file2=None, output=None):
        self.ms_file1, self.ms_file2, self.compound_list, self.output = ms_file1, ms_file2, compound_list, output

    @property
    def get_rt(self):
        if ".xlsx" in self.compound_list or '.XLSX' in self.compound_list:
            data = pd.DataFrame(
                pd.read_excel(self.compound_list, usecols=[u'Compound', u'RT', u'Index', u'IS_correction']))
        if ".csv" in self.compound_list or '.CSV' in self.compound_list:
            data = pd.DataFrame(
                pd.read_csv(self.compound_list, usecols=[u'Compound', u'RT', u'Index', u'IS_correction']))

        com_rt_dict = {i[0]: i[1] for i in zip([i for i in data['Compound']], [i for i in data['RT']])}
        x, y = GetAllRt(compound_list=self.compound_list, ms_file=self.ms_file1), \
               GetAllRt(compound_list=self.compound_list, ms_file=self.ms_file2)

        rt_dict1, rt_dict2, d = x.calculate_single_rt(), y.calculate_single_rt(), {}
        single_nearest_rt_dict1, single_nearest_rt_dict2 = x.single_nearest_rt, y.single_nearest_rt

        f = {}  # f是单物质 0.1 - 0.15
        for i in rt_dict1.keys():
            if i in rt_dict2.keys():
                if 'carnitine' not in i.lower() or i == 'Stearylcarnitine':
                    # 如果标品7和8之间的差异小于0.02，则认为二者rt是一样的
                    if abs(float(rt_dict1[i]) - float(rt_dict2[i])) <= 0.02:
                        if abs(com_rt_dict[i] - (float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5) <= 0.1:
                            d[i] = round((float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5, 3)
                        if 0.1 < abs(com_rt_dict[i] - (float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5) <= 0.15:
                            f[i] = round((float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5, 3)
                    else:
                        if (abs(float(rt_dict1[i]) - com_rt_dict[i])) > abs((float(rt_dict2[i]) - com_rt_dict[i])):
                            if (abs(float(rt_dict2[i]) - com_rt_dict[i])) <= 0.1:
                                d[i] = float(rt_dict2[i])
                            if 0.1 < (abs(float(rt_dict2[i]) - com_rt_dict[i])) <= 0.15:
                                f[i] = float(rt_dict2[i])

                        if (abs(float(rt_dict1[i]) - com_rt_dict[i])) <= abs((float(rt_dict2[i]) - com_rt_dict[i])):
                            if (abs(float(rt_dict1[i]) - com_rt_dict[i])) <= 0.1:
                                d[i] = float(rt_dict1[i])
                            if 0.1 < (abs(float(rt_dict1[i]) - com_rt_dict[i])) <= 0.15:
                                f[i] = float(rt_dict1[i])
                else:

                    if abs(float(rt_dict1[i]) - float(rt_dict2[i])) <= 0.02:
                        if abs(com_rt_dict[i] - (float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5) <= 0.15:
                            d[i] = round((float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5, 3)
                        if 0.15 < abs(com_rt_dict[i] - (float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5) <= 0.3:
                            f[i] = round((float(rt_dict1[i]) + float(rt_dict2[i])) * 0.5, 3)

        for i in x.single:
            if i not in d and i not in f:
                if i in single_nearest_rt_dict1 and i in single_nearest_rt_dict2:
                    if abs(single_nearest_rt_dict1[i] - single_nearest_rt_dict2[i]) <= 0.02:
                        rt1 = (single_nearest_rt_dict1[i] + single_nearest_rt_dict2[i]) * 0.5
                        rt1 = round(rt1, 2)
                        if rt1 - com_rt_dict[i] < 0.1:
                            d[i] = rt1
                        if 0.1 <= rt1 - com_rt_dict[i] <= 0.15:
                            f[i] = rt1

        rt_dict3, rt_dict4 = x.calculate_multi_rt(), y.calculate_multi_rt()
        e = {}  # e多物质0-0.1
        g = {}  # g多物质0.1-0.15
        for i in rt_dict3.keys():
            if i in rt_dict4.keys():
                if 'carnitine' not in i.lower() or i == 'Stearylcarnitine':
                    if abs(float(rt_dict3[i]) - float(rt_dict4[i])) <= 0.02:
                        if abs(com_rt_dict[i] - (float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5) <= 0.1:
                            e[i] = round((float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5, 3)

                        if 0.1 < abs(com_rt_dict[i] - (float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5) <= 0.15:
                            g[i] = round((float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5, 3)

                    else:
                        if (abs(float(rt_dict3[i]) - com_rt_dict[i])) > abs((float(rt_dict4[i]) - com_rt_dict[i])):
                            if (abs(float(rt_dict4[i]) - com_rt_dict[i])) <= 0.1:
                                e[i] = float(rt_dict4[i])
                            if 0.1 < (abs(float(rt_dict4[i]) - com_rt_dict[i])) <= 0.15:
                                g[i] = float(rt_dict4[i])

                        if (abs(float(rt_dict3[i]) - com_rt_dict[i])) <= abs((float(rt_dict4[i]) - com_rt_dict[i])):
                            if (abs(float(rt_dict3[i]) - com_rt_dict[i])) <= 0.1:
                                e[i] = float(rt_dict3[i])
                            if 0.1 < (abs(float(rt_dict3[i]) - com_rt_dict[i])) <= 0.15:
                                g[i] = float(rt_dict3[i])

                else:
                    if abs(float(rt_dict3[i]) - float(rt_dict4[i])) <= 0.02:
                        if abs(com_rt_dict[i] - (float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5) <= 0.15:
                            e[i] = round((float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5, 3)

                        if 0.15 < abs(com_rt_dict[i] - (float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5) <= 0.3:
                            g[i] = round((float(rt_dict3[i]) + float(rt_dict4[i])) * 0.5, 3)

        return d, e, f, g

    def modify_test(self):
        if ".xlsx" in self.compound_list or '.XLSX' in self.compound_list:
            a = pd.read_excel(io=self.compound_list)
        if ".csv" in self.compound_list or '.CSV' in self.compound_list:
            a = pd.read_csv(self.compound_list)
        all_lst = []
        head = list(a.columns)
        all_lst.append(head)
        for i in np.array(a):
            all_lst.append(list(i))
        rt_dict, rt_dict1, rt_dict2, rt_dict3 = self.get_rt
        compound_lst, compound_lst1, compound_lst2, compound_lst3 = rt_dict.keys(), rt_dict1.keys(), rt_dict2.keys(), rt_dict3.keys()
        import xlsxwriter
        workbook = xlsxwriter.Workbook(self.output)
        worksheet = workbook.add_worksheet(name='data')
        for i in range(0, len(all_lst)):
            j = all_lst[i]
            if j[1] in compound_lst:
                bold = workbook.add_format({
                    'valign': 'vcenter',  # 垂直对齐方式
                    'fg_color': '#F4B084',  # 单元格背景颜色
                })
                j[4] = str(rt_dict[j[1]])
                j[7], j[5], j[6] = 'Largest', '0.05', '0.05'
                for k in range(len(j)):
                    worksheet.write(i, k, str(j[k]).replace('?', '').strip(), bold)

            elif j[1] in compound_lst1:
                j[4] = str(rt_dict1[j[1]])
                j[7], j[5], j[6] = 'Largest', '0.05', '0.05'
                bold1 = workbook.add_format({
                    'valign': 'vcenter',  # 垂直对齐方式
                    'fg_color': '#70CD96',  # 单元格背景颜色
                })
                for k in range(len(j)):
                    worksheet.write(i, k, str(j[k]).replace('?', '').strip(), bold1)

            elif j[1] in compound_lst2:
                j[4] = str(rt_dict2[j[1]])
                j[7], j[5], j[6] = 'Largest', '0.05', '0.05'
                bold1 = workbook.add_format({
                    'valign': 'vcenter',  # 垂直对齐方式
                    'fg_color': 'FF6A33',  # 单元格背景颜色
                })
                for k in range(len(j)):
                    worksheet.write(i, k, str(j[k]).replace('?', '').strip(), bold1)

            elif j[1] in compound_lst3:
                j[4] = str(rt_dict3[j[1]])
                j[7], j[5], j[6] = 'Largest', '0.05', '0.05'
                bold1 = workbook.add_format({
                    'valign': 'vcenter',  # 垂直对齐方式
                    'fg_color': '#007099',  # 单元格背景颜色
                })
                for k in range(len(j)):
                    worksheet.write(i, k, str(j[k]).replace('?', '').strip(), bold1)
            else:
                for k in range(len(j)):
                    worksheet.write(i, k, str(j[k]).replace('?', '').strip())
        workbook.close()


class GUI(object):
    def __init__(self):
        self.root = Tk()
        self.root.title("Rentention time adjust")
        self.root.geometry(size)
        notebook = Notebook(self.root)
        frame1 = Frame(notebook)
        self.root.rowconfigure(1, weight=1)
        self.root.rowconfigure(2, weight=8)
        var_save = StringVar()
        var_comp = StringVar()
        self.entry1 = Entry(frame1, textvariable=var_comp, width=50, font=40)
        self.entry1.place(x=90, y=60)
        self.entry2 = Entry(frame1, textvariable=var_save, width=50, font=40)
        self.entry2.place(x=90, y=120)
        button_save = Button(frame1, text="Save", command=self.save)
        button_save.place(x=10, y=120)
        button_save = Button(frame1, text="comp", command=self.select)
        button_save.place(x=10, y=60)
        Button(frame1, text="Add", command=self.add_msfile).place(x=40, y=350)
        Button(frame1, text="Remove", command=self.delete).place(x=235, y=350)
        Button(frame1, text="start", command=self.thread).place(x=430, y=350)
        r = StringVar()
        self.listbox_filename = Listbox(frame1, listvariable=r, selectmode=EXTENDED, width=70, height=5)
        self.listbox_filename.place(x=10, y=180)
        bar = Scrollbar(self.root)
        bar.pack(side=RIGHT, fill=Y)
        self.listbox_filename['yscrollcommand'] = bar.set
        bar['command'] = self.listbox_filename.yview
        notebook.add(frame1, text="Retention time")
        notebook.pack(expand=1, fill="both")
        self.root.resizable(0, 0)
        self.root.mainloop()

    def save(self):
        self.entry2.delete(0, END)
        a = filedialog.asksaveasfilename(title="save files", filetypes=[("XLSX", ".xlsx")])
        self.entry2.insert(0, a + '.xlsx')

    def select(self):
        self.entry1.delete(0, END)
        a = filedialog.askopenfilename(title="select compound_list", filetypes=[("XLSX", ".xlsx"), ("CSV", ".csv")])
        self.entry1.insert(0, a)

    def add_msfile(self):
        txt = filedialog.askopenfilenames(filetypes=[(".TXT", ".txt")])
        for i in txt:
            if i not in self.listbox_filename.get(0, END):
                self.listbox_filename.insert(END, i)

    def delete(self):
        x = self.listbox_filename.curselection()
        for i in x:
            y = x.index(i)
            z = int(i) - y
            self.listbox_filename.delete(z)

    def start(self):
        import time
        time1 = time.time()
        print('Start at:%s' % time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))
        A = ModifyCsv(compound_list=self.entry1.get(), ms_file1=self.listbox_filename.get(0, END)[0],
                      ms_file2=self.listbox_filename.get(0, END)[1], output=self.entry2.get())
        A.modify_test()
        time2 = time.time()
        spent_time = int(time2 - time1)
        print('Finish at:%s' % time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))
        print('Used time: %ds' % spent_time)

    def thread(self):
        job = threading.Thread(target=self.start)
        job.setDaemon(True)
        job.start()


if __name__ == '__main__':
    GUI()
